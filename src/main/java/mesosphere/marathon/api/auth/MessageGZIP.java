package mesosphere.marathon.api.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * GZIP压缩解压类 */
public class MessageGZIP {
	
	private static final Logger log = LoggerFactory.getLogger(MessageGZIP.class);
	
	private static String encode = "utf-8";// "ISO-8859-1"

	public String getEncode() {
		return encode;
	}

	/* * 设置 编码，默认编码：UTF-8 */
	public void setEncode(String encode) {
		MessageGZIP.encode = encode;
	}

	/* * 字符串压缩为字节数组 */
	public static byte[] compressToByte(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(encode));
			gzip.close();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return out.toByteArray();
	}

	/* * 字符串压缩为字节数组 */
	public static byte[] compressToByte(String str, String encoding) {
		if (str == null || str.length() == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(encoding));
			gzip.close();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return out.toByteArray();
	}

	/* * 字节数组解压缩后返回字符串 */
	public static String uncompressToString(byte[] b) {
		if (b == null || b.length == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		try {
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return out.toString();
	}

	/* * 字节数组解压缩后返回字符串 */
	public static String uncompressToString(byte[] b, String encoding) {
		if (b == null || b.length == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		try {
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			return out.toString(encoding);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return null;
	}
}