package com.wwgroup.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ToolSet {
	public static byte[] InputStreamToByte(InputStream is) {
		try {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		}
		catch (IOException ex) {
			System.out.println("附件存储出现异常：：：" + ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}
}
