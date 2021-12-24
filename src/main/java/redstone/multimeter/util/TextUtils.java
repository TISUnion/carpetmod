package redstone.multimeter.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TextUtils {
	
	private static final int MAX_WIDTH = 200;
	
	public static List<ITextComponent> toLines(FontRenderer font, String text) {
		List<ITextComponent> lines = new ArrayList<>();
		
		while (!text.isEmpty()) {
			int lastSpace = -1;
			int length = 0;
			
			while (length++ < text.length()) {
				if (length == text.length()) {
					break;
				}
				
				int index = length - 1;
				
				if (text.charAt(index) == ' ') {
					lastSpace = index;
				}
				
				String subString = text.substring(0, length);
				
				if (font.getStringWidth(subString) > MAX_WIDTH) {
					if (lastSpace >= 0) {
						subString = text.substring(0, lastSpace);
						length = lastSpace + 1;
					}
					
					ITextComponent line = new TextComponentString(subString);
					lines.add(line);
					
					break;
				}
			}
			
			if (length == text.length()) {
				if (length > 0) {
				    ITextComponent line = new TextComponentString(text);
					lines.add(line);
				}
				
				break;
			}
			
			text = text.substring(length);
		}
		
		return lines;
	}
	
	public static void addFancyText(List<ITextComponent> lines, String title, Object info) {
		addFancyText(lines, title, info.toString());
	}
	
	public static void addFancyText(List<ITextComponent> lines, String title, String info) {
		lines.add(formatFancyText(title, info));
	}
	
	public static ITextComponent formatFancyText(String title, String info) {
		return new TextComponentString("").
			appendSibling(new TextComponentString(title + ": ").applyTextStyle(TextFormatting.GOLD)).
			appendSibling(new TextComponentString(info));
	}
}
