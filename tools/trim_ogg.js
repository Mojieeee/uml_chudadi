const fs = require("fs");

const [, , inputPath, durationSecondsArg] = process.argv;
if (!inputPath || !durationSecondsArg) {
  console.error("Usage: node tools/trim_ogg.js <input.ogg> <duration_seconds>");
  process.exit(1);
}

const sampleRate = 44_100;
const endSamples = BigInt(Math.round(Number(durationSecondsArg) * sampleRate));
const source = fs.readFileSync(inputPath);

function buildCrcTable() {
  const table = new Uint32Array(256);
  for (let i = 0; i < 256; i += 1) {
    let value = i << 24;
    for (let j = 0; j < 8; j += 1) {
      value = (value & 0x80000000) ? ((value << 1) ^ 0x04c11db7) : (value << 1);
    }
    table[i] = value >>> 0;
  }
  return table;
}

const crcTable = buildCrcTable();

function oggCrc(buffer) {
  let crc = 0;
  for (const byte of buffer) {
    crc = (((crc << 8) >>> 0) ^ crcTable[((crc >>> 24) ^ byte) & 0xff]) >>> 0;
  }
  return crc >>> 0;
}

function readGranule(buffer, offset) {
  return buffer.readBigInt64LE(offset + 6);
}

function writeGranule(page, value) {
  page.writeBigInt64LE(value, 6);
}

function parsePages(buffer) {
  const pages = [];
  let offset = 0;
  while (offset < buffer.length) {
    if (buffer.toString("ascii", offset, offset + 4) !== "OggS") {
      throw new Error(`Invalid Ogg page at byte ${offset}`);
    }
    const segmentCount = buffer[offset + 26];
    const headerLength = 27 + segmentCount;
    let bodyLength = 0;
    for (let i = 0; i < segmentCount; i += 1) {
      bodyLength += buffer[offset + 27 + i];
    }
    const pageLength = headerLength + bodyLength;
    pages.push({
      offset,
      pageLength,
      granule: readGranule(buffer, offset),
      headerType: buffer[offset + 5],
      lastSegment: segmentCount > 0 ? buffer[offset + 27 + segmentCount - 1] : 0,
    });
    offset += pageLength;
  }
  return pages;
}

const pages = parsePages(source);
const originalLastPage = pages[pages.length - 1];
const targetIndex = pages.findIndex(
  (page) => page.granule >= endSamples && page.lastSegment < 255
);

if (targetIndex < 0) {
  throw new Error("Could not find a complete Ogg page at or after the requested trim point.");
}

const finalPage = pages[targetIndex];
const output = Buffer.from(source.subarray(0, finalPage.offset + finalPage.pageLength));
output[finalPage.offset + 5] |= 0x04;
writeGranule(output.subarray(finalPage.offset, finalPage.offset + finalPage.pageLength), endSamples);
output.writeUInt32LE(0, finalPage.offset + 22);
output.writeUInt32LE(
  oggCrc(output.subarray(finalPage.offset, finalPage.offset + finalPage.pageLength)),
  finalPage.offset + 22
);
fs.writeFileSync(inputPath, output);

const trimmedPages = parsePages(output);
const trimmedLastPage = trimmedPages[trimmedPages.length - 1];
console.log(
  JSON.stringify(
    {
      inputPath,
      originalBytes: source.length,
      trimmedBytes: output.length,
      originalSeconds: Number(originalLastPage.granule) / sampleRate,
      trimmedSeconds: Number(trimmedLastPage.granule) / sampleRate,
      originalPages: pages.length,
      trimmedPages: trimmedPages.length,
      finalHasEos: Boolean(trimmedLastPage.headerType & 0x04),
    },
    null,
    2
  )
);
