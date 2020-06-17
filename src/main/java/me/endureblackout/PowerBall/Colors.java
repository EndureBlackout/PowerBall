package me.endureblackout.PowerBall;

import org.bukkit.Color;

public class Colors {

	public String color = "";

	public Colors(String color) {
		this.color = color;
	}

	public Color getColor() {
		if (color.equalsIgnoreCase("Aqua")) {
			return Color.AQUA;
		} else if (color.equalsIgnoreCase("Black")) {
			return Color.BLACK;
		} else if (color.equalsIgnoreCase("Blue")) {
			return Color.BLUE;
		} else if (color.equalsIgnoreCase("Fuchsia")) {
			return Color.FUCHSIA;
		} else if (color.equalsIgnoreCase("Gray")) {
			return Color.GRAY;
		} else if (color.equalsIgnoreCase("Green")) {
			return Color.GREEN;
		} else if (color.equalsIgnoreCase("Lime")) {
			return Color.LIME;
		} else if (color.equalsIgnoreCase("Maroon")) {
			return Color.MAROON;
		} else if (color.equalsIgnoreCase("Navy")) {
			return Color.NAVY;
		} else if (color.equalsIgnoreCase("Olive")) {
			return Color.OLIVE;
		} else if (color.equalsIgnoreCase("Orange")) {
			return Color.ORANGE;
		} else if (color.equalsIgnoreCase("Purple")) {
			return Color.PURPLE;
		} else if (color.equalsIgnoreCase("Red")) {
			return Color.RED;
		} else if (color.equalsIgnoreCase("Silver")) {
			return Color.SILVER;
		} else if (color.equalsIgnoreCase("Teal")) {
			return Color.TEAL;
		} else if (color.equalsIgnoreCase("White")) {
			return Color.WHITE;
		} else if (color.equalsIgnoreCase("Yellow")) {
			return Color.YELLOW;
		} else if (color.equalsIgnoreCase("Pink")) {
			return Color.fromRGB(255, 192, 203);
		} else if (color.equalsIgnoreCase("Light Blue")) {
			return Color.fromRGB(30, 144, 255);
		} else if (color.equalsIgnoreCase("BlueViolet")) {
			return Color.fromRGB(138, 43, 226);
		} else if (color.equalsIgnoreCase("Steel Blue")) {
			return Color.fromRGB(240, 248, 255);
		} else if (color.equalsIgnoreCase("Gabe")) {
			return Color.fromRGB(37, 162, 138);
		}

		return null;
	}
}
