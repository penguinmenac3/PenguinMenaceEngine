/**
 * 
 */
package net.pme.model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Loads textures if they are not already loaded into the memory.
 * 
 * @author Michael Fürst
 * @version 1.0
 */
public class TextureLoader {
	private static HashMap<String, Integer> textures = new HashMap<String, Integer>();
	private static HashMap<Integer, Integer> openedTimes = new HashMap<Integer, Integer>();

	/**
	 * Load a texture from a file. When already loaded before it will only
	 * return a pointer to the texture.
	 * 
	 * @param img
	 *            The image you want as a texture.
	 * @return The texture identifier.
	 */
	public static int loadFromFile(String pathname) throws IOException {
		if (!textures.containsKey(pathname) || openedTimes.get(textures.get(pathname)) == 0) {
			textures.put(pathname,
					loadTextureForceReload(ImageIO.read(new File(pathname))));
			openedTimes.put(textures.get(pathname), 0);
		}
		openedTimes.put(textures.get(pathname), openedTimes.get(textures.get(pathname)) + 1);
		return textures.get(pathname);
	}

	/**
	 * Load a texture from a buffered image, forcing it to reload. Extremely
	 * memory hungry and slow.
	 * 
	 * @param img
	 *            The image you want as a texture.
	 * @return The texture identifier.
	 */
	public static int loadTextureForceReload(BufferedImage img) {
		byte[] src = ((DataBufferByte) img.getData().getDataBuffer()).getData();

		bgr2rgb(src);

		ByteBuffer pixels = (ByteBuffer) BufferUtils
				.createByteBuffer(src.length).put(src, 0x00000000, src.length)
				.flip();

		IntBuffer textures = BufferUtils.createIntBuffer(0x00000001);

		GL11.glGenTextures(textures);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(0x00000000));

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0x00000000, GL11.GL_RGB,
				img.getWidth(), img.getHeight(), 0x00000000, GL11.GL_RGB,
				GL11.GL_UNSIGNED_BYTE, pixels);

		return textures.get(0x00000000);
	}

	private static void bgr2rgb(byte[] target) {
		byte tmp;
		for (int i = 0x00000000; i < target.length; i += 0x00000003) {
			tmp = target[i];
			target[i] = target[i + 0x00000002];
			target[i + 0x00000002] = tmp;
		}
	}

	/**
	 * Frees the memory textures take. For each loadTexture texture there must
	 * be one free call of free before the memory is actually released.
	 * 
	 * @param textureId The texture id of the texture to delete.
	 */
	public static void free(int textureId) {
		if (openedTimes.get(textureId) - 1 < 0) {
			throw new RuntimeException("More textures removed than loaded");
		}
		openedTimes.put(textureId, openedTimes.get(textureId) - 1);
		if (openedTimes.get(textureId) == 0) {
			GL11.glDeleteTextures(textureId);
		}
	}
	
	/**
	 * Forces a texture to be removed from memory.
	 * Should be used for textures loaded withloadTextureForceReload.
	 * Should <b>not</b> be used for textures loaded with loadFromFile.
	 * 
	 * @param textureId The texture id of the texture to delete.
	 */
	public static void forceFree(int textureId) {
		GL11.glDeleteTextures(textureId);
	}
}