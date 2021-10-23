package carpet.utils.deobfuscator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * 	Mapping from https://github.com/lucko/spark-mappings
 * 	https://github.com/lucko/spark-mappings/blob/1042923655ff397bf0e8d95131212519bbe9ebe8/dist/1_13_2/mcp.json
 */
public class McpMapping
{
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static MappingFile mappingFile = null;

	public static void init()
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		try
		{
			InputStream inputStream = McpMapping.class.getClassLoader().getResourceAsStream("assets/carpet/mcp.json");
			if (inputStream == null)
			{
				throw new FileNotFoundException("Failed to read mapping file");
			}
			mappingFile = gson.fromJson(new InputStreamReader(inputStream), MappingFile.class);
			mappingFile.classes.values().forEach(ClassMapping::buildMap);
		}
		catch (Exception e)
		{
			LOGGER.info("Failed to load mapping");
			e.printStackTrace();
		}
	}

	public static Optional<String> remapClass(String className)
	{
		if (mappingFile == null || !mappingFile.classes.containsKey(className))
		{
			return Optional.empty();
		}
		return Optional.of(mappingFile.classes.get(className).name);
	}

	public static Optional<String> remapMethod(String className, String methodName, int lineNumber)
	{
		if (mappingFile == null || !mappingFile.classes.containsKey(className))
		{
			return Optional.empty();
		}
		try
		{
			String methodDesc = getMethodDesc(className, methodName, lineNumber);
			if (methodDesc != null)
			{
				return remapMethod(className, methodName, methodDesc);
			}
		}
		catch (IOException ignored)
		{
		}
		ClassMapping classMapping = mappingFile.classes.get(className);
		List<MethodMapping> possibleMethods = classMapping.methodListMap.get(methodName);
		if (possibleMethods != null && possibleMethods.size() == 1)
		{
			return Optional.of(possibleMethods.get(0).name);
		}
		return Optional.empty();
	}

	public static Optional<String> remapMethod(String className, String methodName, String methodDesc)
	{
		if (mappingFile == null || !mappingFile.classes.containsKey(className))
		{
			return Optional.empty();
		}
		ClassMapping classMapping = mappingFile.classes.get(className);
		MethodMapping methodMapping = classMapping.methodWithDescMap.get(methodName + methodDesc);
		return Optional.of(Optional.ofNullable(methodMapping).map(m -> m.name).orElse(methodName));
	}

	private static class MappingFile
	{
		@Expose @SerializedName("c")
		public Map<String, ClassMapping> classes;
	}

	private static class ClassMapping
	{
		@Expose @SerializedName("o")
		public String obfuscated;

		@Expose @SerializedName("n")
		public String name;

		@Expose @SerializedName("m")
		public MethodMapping[] methods;

		// (obfuscated + desc) -> Mapping
		@Expose(deserialize = false)
		public Map<String, MethodMapping> methodWithDescMap = Maps.newHashMap();

		// obfuscated -> List<Mapping>
		@Expose(deserialize = false)
		public Map<String, List<MethodMapping>> methodListMap = Maps.newHashMap();

		public void buildMap()
		{
			Arrays.stream(this.methods).forEach(m -> {
				this.methodWithDescMap.put(m.obfuscated + m.description, m);
				this.methodListMap.computeIfAbsent(m.obfuscated, name -> Lists.newArrayList()).add(m);
			});
		}
	}

	private static class MethodMapping
	{
		@Expose @SerializedName("o")
		public String obfuscated;

		@Expose @SerializedName("n")
		public String name;

		@Expose @SerializedName("d")
		public String description;
	}

	// Stuffs below are stolen from carpetmod112

	private static final Map<String, String> methodDescCache = Maps.newHashMap();

	private static String pack(String className, String methodName, int lineNumber)
	{
		return className + "/" + methodName + "@" + lineNumber;
	}

	private static String getMethodDesc(String className, String methodName, int lineNumber) throws IOException
	{
		className = className.replace('.', '/');
		String key = pack(className, methodName, lineNumber);
		if (methodDescCache.containsKey(key))
		{
			return methodDescCache.get(key);
		}

		// Java Class File Format:
		// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html

		InputStream is = StackTraceDeobfuscator.class.getClassLoader().getResourceAsStream(className + ".class");
		if (is == null)
			return null;
		DataInputStream dataIn = new DataInputStream(is);
		skip(dataIn, 8); // header

		// constant pool
		Map<Integer, String> stringConstants = new HashMap<>();
		int cpCount = dataIn.readUnsignedShort();
		int[] constSizes = {-1, -1, -1, 4, 4, 8, 8, 2, 2, 4, 4, 4, 4, -1, -1, 3, 2, -1, 4};
		for (int cpIndex = 1; cpIndex < cpCount; cpIndex++)
		{
			int tag = dataIn.readUnsignedByte();
			if (tag == 1)
			{ // CONSTANT_Utf8
				stringConstants.put(cpIndex, dataIn.readUTF());
				//System.out.println(cpIndex + " -> " + stringConstants.get(cpIndex));
			}
			else
			{
				if (tag == 5 || tag == 6)
				{ // CONSTANT_Long or CONSTANT_Double
					cpIndex++;
				}
				skip(dataIn, constSizes[tag]);
			}
		}

		skip(dataIn, 6); // more boring information

		// Need to know interface count to know how much to skip over
		int interfaceCount = dataIn.readUnsignedShort();
		skip(dataIn, interfaceCount * 2);

		// Skip over the fields
		int fieldCount = dataIn.readUnsignedShort();
		for (int i = 0; i < fieldCount; i++)
		{
			skip(dataIn, 6);
			int attrCount = dataIn.readUnsignedShort();
			for (int j = 0; j < attrCount; j++)
			{
				skip(dataIn, 2);
				long length = Integer.toUnsignedLong(dataIn.readInt());
				skip(dataIn, length);
			}
		}

		// Methods, now we're talking
		int methodCount = dataIn.readUnsignedShort();
		for (int i = 0; i < methodCount; i++)
		{
			skip(dataIn, 2); // access
			String name = stringConstants.get(dataIn.readUnsignedShort());
			String desc = stringConstants.get(dataIn.readUnsignedShort());
			int attrCount = dataIn.readUnsignedShort();
			for (int j = 0; j < attrCount; j++)
			{
				String attrName = stringConstants.get(dataIn.readUnsignedShort());
				long length = Integer.toUnsignedLong(dataIn.readInt());
				if (name.equals(methodName) && attrName.equals("Code"))
				{
					skip(dataIn, 4); // max stack + locals
					long codeLength = Integer.toUnsignedLong(dataIn.readInt());
					skip(dataIn, codeLength);
					int exceptionTableLength = dataIn.readUnsignedShort();
					skip(dataIn, exceptionTableLength * 8);
					int codeAttrCount = dataIn.readUnsignedShort();
					for (int k = 0; k < codeAttrCount; k++)
					{
						String codeAttrName = stringConstants.get(dataIn.readUnsignedShort());
						long codeAttrLength = Integer.toUnsignedLong(dataIn.readInt());
						if (codeAttrName.equals("LineNumberTable"))
						{
							int lineNumberTableLength = dataIn.readUnsignedShort();
							for (int l = 0; l < lineNumberTableLength; l++)
							{
								skip(dataIn, 2); // start_pc
								int lineNo = dataIn.readUnsignedShort();
								if (lineNo == lineNumber)
								{
									methodDescCache.put(key, desc);
									return desc;
								}
							}
						}
						else
						{
							skip(dataIn, codeAttrLength);
						}
					}
				}
				else
				{
					skip(dataIn, length);
				}
			}
		}

		return null;
	}

	private static void skip(DataInputStream dataIn, long n) throws IOException
	{
		long actual = 0;
		while (actual < n)
		{
			actual += dataIn.skip(n - actual);
		}
	}
}
