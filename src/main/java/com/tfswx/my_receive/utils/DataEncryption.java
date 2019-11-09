package com.tfswx.my_receive.utils;


import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件加解密工具
 * 
 * @author swx
 *
 * @since 2017/12/1
 */
public class DataEncryption {


	/**
	 * 默认密钥
	 */
	private static final byte[] KEY = { 10, 13, 39, 27, 32, 12, 63,
			55 };

	/**
	 * 对字节做异或操作
	 * ◀
	 * @param b
	 *            原始字节值
	 * @param key
	 *            密钥值
	 * @return 计算后的字节值
	 */
	private static byte byteXor(byte b, byte key) {
		return (byte) (b ^ key);
	}

	/**
	 * 对字节数组做异或操作
	 * 
	 * @param data
	 *            原始数组字节值
	 * @param key
	 *            密钥值
	 * @return 计算后的数组字节值
	 */
	private static byte[] byteXor(byte[] data, byte[] key) {

		int dataLen = data.length;

		if (dataLen == 0) {
			return data;
		}

		int keyLen = key.length;

		for (int i = 0; i < dataLen; i++) {
			data[i] = DataEncryption.byteXor(data[i],
					key[i % keyLen]);
		}

		return data;
	}

	/**
	 * 加密，key必须是2的n次方
	 * 
	 * @param data
	 *            解密数据
	 * @param key
	 *            解密值
	 * @return 解密后的值
	 */
	public static byte[] encryption(byte[] data, byte[] key) {
		return DataEncryption.byteXor(data, key);
	}

	/**
	 * 加密, 使用内置密钥
	 * 
	 * @param data
	 *            加密数据
	 * @return 加密后的数据
	 */
	public static byte[] encryption(byte[] data) {
		return DataEncryption.byteXor(data, KEY);
	}

	/**
	 * 加密文件
	 * 
	 * @param srcFile
	 *            明文文 件
	 * @param descFile
	 *            密文文件
	 * @param key
	 *            密钥
	 */
	public static void encryption(String srcFile, String descFile,
			byte[] key) {

		int keyLength = key.length;

		final int bufferSize = 1024 * 512;

		// 读取数据缓存池
		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

		try {
			OutputStream newFile = Files
					.newOutputStream(Paths.get(descFile));

			SeekableByteChannel byteChannel = Files
					.newByteChannel(Paths.get(srcFile));

			int readSize = byteChannel.read(byteBuffer);
			byte[] data = byteBuffer.array();

			while (readSize >= 1) {

				data = byteBuffer.array();

				for (int i = 0; i < readSize; i++) {
					data[i] = DataEncryption.byteXor(data[i],
							key[i % keyLength]);
				}

				newFile.write(data, 0, readSize);
				newFile.flush();

				readSize = byteChannel.read(byteBuffer);
			}
			byteBuffer.clear();
			byteChannel.close();
			newFile.flush();
			newFile.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static byte[] ByteXor(byte[] data) {
		return ByteXor(data,KEY);
	}
	private static byte[] ByteXor(byte[] data, byte[] key) {
		int keyLength = key.length;
		int dataLen = data.length;
		if (dataLen == 0) {
			return data;
		}
		for (int i = 0; i < dataLen; i++) {
			data[i] = DataEncryption.byteXor(data[i], key[i % keyLength]);
		}
		return data;
	}


	/**
	 * 加密文件, 使用内置密钥
	 * 
	 * @param srcFile
	 *            明文文件
	 * @param descFile
	 *            密文文件
	 */
	public static void encryption(String srcFile, String descFile) {
		DataEncryption.encryption(srcFile, descFile,
				DataEncryption.KEY);
	}

	/**
	 * 解密
	 * 
	 * @param data
	 *            数据
	 * @param key
	 *            密钥值
	 * @return 解密后的数据
	 */
	public static byte[] decryption(byte[] data, byte[] key) {
		return DataEncryption.byteXor(data, key);
	}

	/**
	 * 使用内置密钥解密
	 * 
	 * @param data
	 *            数据
	 * @return 解密后的数据
	 */
	public static byte[] decryption(byte[] data) {
		return DataEncryption.byteXor(data, DataEncryption.KEY);
	}

	/**
	 * 解密文件
	 * 
	 * @param srcFile
	 *            密文文件
	 * @param descFile
	 *            解密后的文件
	 * @param key
	 *            密钥
	 */
	public static void decryption(String srcFile, String descFile,
			byte[] key) {
		DataEncryption.encryption(srcFile, descFile, key);
	}

	/**
	 * 解密文件, 使用内置密钥
	 * 
	 * @param srcFile
	 *            密文文件
	 * @param descFile
	 *            解密后的文件
	 */
	public static void decryption(String srcFile, String descFile) {
		DataEncryption.decryption(srcFile, descFile,
				DataEncryption.KEY);
	}


}
