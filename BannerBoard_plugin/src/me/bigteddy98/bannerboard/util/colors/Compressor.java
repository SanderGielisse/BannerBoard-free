package me.bigteddy98.bannerboard.util.colors;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compressor {

	private static final ThreadLocal<Deflater> TL_COMPRESSOR = ThreadLocal.withInitial(() -> new Deflater(9));

	private static final ThreadLocal<Inflater> TL_DECOMPRESSOR = ThreadLocal.withInitial(Inflater::new);

	public static byte[] compress(byte[] given) {
		Deflater compressor = TL_COMPRESSOR.get();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		compressor.setInput(given);
		compressor.finish();
		byte[] buffer = new byte[1024 * 8];
		do {
			int size = compressor.deflate(buffer);
			out.write(buffer, 0, size);
		} while (!compressor.finished());
		compressor.reset();
		return out.toByteArray();
	}

	public static byte[] decompress(byte[] compressed) {
		Inflater decompressor = TL_DECOMPRESSOR.get();

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			decompressor.setInput(compressed);
			byte[] buffer = new byte[1024 * 8];
			do {
				int size = decompressor.inflate(buffer);
				out.write(buffer, 0, size);
			} while (!decompressor.finished());
			decompressor.reset();
			return out.toByteArray();
		} catch (DataFormatException e) {
			throw new RuntimeException(e);
		}
	}
}
