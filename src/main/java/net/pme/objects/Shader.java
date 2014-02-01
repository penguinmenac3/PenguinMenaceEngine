/**
 * 
 */
package net.pme.objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * Shader binding to manage them.
 * 
 * @author Michael Fürst
 * @version 1.0
 */
public class Shader extends GameObject {
	private static final int UNIFORM4F_LENGTH = 4;
	private static final String defaultVSH = 
			"#version 140\n" +
			"in vec3 vertexpos;\n" + 
			"in vec3 texCoords;\n" + 
			"in vec3 normals;\n"+
			"out vec3 VertexPos;\n" + 
			"out vec3 TexCoord;\n" + 
			"out vec3 Normal;\n" + 
			"uniform mat4 MVP;\n" +
			"uniform mat4 V;\n" +
			"void main() {\n" + 
			"gl_Position = MVP * vec4(vertexpos, 1.0);\n" +
			"VertexPos = vertexpos;\n" + 
			"TexCoord = texCoords;\n" + 
			"Normal = normals;\n" + 
			"}\n";
	private static final String defaultFSH = 
			"#version 140\n" +
			"in vec3 VertexPos;\n" + 
			"in vec3 TexCoord;\n" + 
			"in vec3 Normal;\n" +
			"out vec4 color;\n" +
			"void main() {\n" + 
			"color = vec4(1.0,0.0,0.0,1.0);\n"+
			"}\n";
	
	private static Shader defaultShader = null;
	private int vsId = 0;
	private int fsId = 0;
	private int program = 0;
	private HashMap<Integer, float[]> uniforms = new HashMap<Integer, float[]>();

	/**
	 * Create a new shader from files in the given path.
	 * 
	 * There will be automatically laoded path.fsh and path.vsh.
	 * 
	 * @param id The id for the gameengine.
	 * @param path The path to the files, without the file ending. There must be a path.vsh and a path.fsh.
	 */
	public Shader(long id, String path) {
		this(id, readFromFile(path+".vsh"), readFromFile(path+".fsh"));
	}
	
	/**
	 * Load a shader from a file into a string.
	 * @param pathname The file to open.
	 * @return The string read from the file or null if not found.
	 */
	static private String readFromFile(String pathname) {
		File file = new File(pathname);
	    StringBuilder fileContents = new StringBuilder((int)file.length());
		try {
			Scanner scanner = new Scanner(file);
			String lineSeparator = System.getProperty("line.separator");

		    try {
		        while(scanner.hasNextLine()) {        
		            fileContents.append(scanner.nextLine() + lineSeparator);
		        }
		        return fileContents.toString();
		    } finally {
		        scanner.close();
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create a shader with the given id and shader strings.
	 * 
	 * @param id
	 *            The id for the shader.
	 * @param vsh
	 *            The vertex shader. (as a string)
	 * @param fsh
	 *            The fragment shader. (as a string)
	 */
	public Shader(final long id, String vsh, String fsh) {
		super(id);
		if (vsh == null) {
			vsh = defaultVSH;
		}
		if (fsh == null) {
			fsh = defaultFSH;
		}
		try {
			if (vsh != null) {
				vsId = createShader(vsh, ARBVertexShader.GL_VERTEX_SHADER_ARB);
			}
			if (fsh != null) {
				fsId = createShader(fsh,
						ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
			}
			program = ARBShaderObjects.glCreateProgramObjectARB();

			if (program <= 0) {
				throw new RuntimeException("Cannot create shader");
			}

			if (vsId != 0) {
				ARBShaderObjects.glAttachObjectARB(program, vsId);
			}
			if (fsId != 0) {
				ARBShaderObjects.glAttachObjectARB(program, fsId);
			}
			ARBShaderObjects.glLinkProgramARB(program);

			if (ARBShaderObjects.glGetObjectParameteriARB(program,
					ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
				throw new RuntimeException(getLogInfo(program));
			}

			ARBShaderObjects.glValidateProgramARB(program);
			if (ARBShaderObjects.glGetObjectParameteriARB(program,
					ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
				throw new RuntimeException(getLogInfo(program));
			}
		} catch (RuntimeException e) {
			delete();
			throw e;
		}
	}

	/**
	 * Create a default shader for an object without shader.
	 */
	public Shader() {
		this(0, defaultVSH, defaultFSH);
	}

	/**
	 * Make the shader active for all triangles drawn after this call. (Be
	 * careful when calling this. Objects of the Engine automatically call this
	 * if they have a shader attached.)
	 */
	public final void bind() {
		if (program > 0) {
			ARBShaderObjects.glUseProgramObjectARB(program);
			updateUniforms();
		} else {
			throw new RuntimeException("Cannot bind when shader was deleted!");
		}
	}

	/**
	 * Make the shader inactive. Technically activating a default shader. (Be
	 * careful when calling this. Objects of the Engine automatically call this
	 * if they have a shader attached.)
	 */
	public final void unbind() {
		ARBShaderObjects.glUseProgramObjectARB(0);
	}

	/**
	 * Bind a java array to be a uniform4f in your shader code. In the shader it
	 * is used by "uniform vec4 uniform_name;"
	 * 
	 * @param uniform
	 *            The name of the uniform.
	 * @param value
	 *            The array of size 4 which to bind to that uniform.
	 * @return Weather binding was successful or not.
	 */
	public final boolean setUniform4f(final String uniform, final float[] value) {
		boolean result = false;

		if (uniform != null && program > 0 && GL20.glGetUniformLocation(program, uniform) >= 0
				&& value != null && value.length == UNIFORM4F_LENGTH) {
			uniforms.put(GL20.glGetUniformLocation(program, uniform), value);
			result = true;
		}

		return result;
	}

	/**
	 * Remove a uniform binding.
	 * 
	 * @param uniform
	 *            The uniform to remove.
	 * @return Weather the removal was successful or not. (It is successful as
	 *         long as the uniform was registered before)
	 */
	public final boolean removeUniform4f(final String uniform) {
		return uniforms.remove(GL20.glGetUniformLocation(program, uniform)) != null;
	}

	/**
	 * Delete the shader.
	 * 
	 * Should be called every time before you null an object.
	 * 
	 * This automatically removes all uniform bindings, whereas they are no
	 * longer usefull.
	 */
	public final void delete() {
		Set<Integer> keys = uniforms.keySet();
		for (Integer k : keys) {
			uniforms.remove(k);
		}
		if (vsId != 0) {
			ARBShaderObjects.glDeleteObjectARB(vsId);
			vsId = 0;
		}
		if (fsId != 0) {
			ARBShaderObjects.glDeleteObjectARB(fsId);
			fsId = 0;
		}
		if (program != 0) {
			ARBShaderObjects.glDeleteObjectARB(program);
			program = 0;
		}
	}

	/**
	 * Update uniforms.
	 */
	private void updateUniforms() {
		Set<Integer> keys = uniforms.keySet();
		for (Integer k : keys) {
			float[] value = uniforms.get(k);
			GL20.glUniform4f(k, value[0],
					value[1], value[2], value[3]);
		}
	}

	/**
	 * Create a shader.
	 * @param shaderString The string describing the shader.
	 * @param shaderType The type of the shader.
	 * @return The int to the shader in gl.
	 */
	private int createShader(final String shaderString, final int shaderType) {
		int shader = 0;
		shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
		if (shader == 0) {
			return 0;
		}
		ARBShaderObjects.glShaderSourceARB(shader, shaderString);
		ARBShaderObjects.glCompileShaderARB(shader);

		if (ARBShaderObjects.glGetObjectParameteriARB(shader,
				ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
			throw new RuntimeException("Error creating shader: "
					+ getLogInfo(shader));
		}

		return shader;
	}

	/**
	 * Get the log info for a shader object.
	 * @param obj The shader for which to get the log.
	 * @return The log info.
	 */
	private String getLogInfo(final int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects
				.glGetObjectParameteriARB(obj,
						ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	public int getProgram() {
		return program;
	}

	public static Shader getDefaultShader() {
		if (defaultShader == null) {
			defaultShader = new Shader();
		}
		return defaultShader;
	}
}
