package com.nisovin.magicspells.spelleffects.effecttypes;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.TimeUtil;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.SpellEffect;

public class FontAnimationEffect extends SpellEffect {

	private String fontNameSpace;
	private String fontName;
	private String titlePart;

	private int interval;
	private int durationTicks;
	private int startFrame;
	private int floorFrame;
	private int ceilingFrame;
	private int delay;

	private boolean fadeOut;
	private boolean reverse;

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		String[] name = config.getString("name", "").split(":");

		if (name.length > 1) {
			fontNameSpace = name[0];
			fontName = name[1];
		} else {
			fontNameSpace = "minecraft";
			fontName = name[0];
		}

		titlePart = config.getString("title-part", "subtitle");
		if (!titlePart.equals("actionbar")) titlePart = "subtitle";

		interval = Math.max(config.getInt("interval", 1), 1);
		durationTicks = (int) Math.floor(Math.max((config.getInt("duration", 20) / interval), 1));
		startFrame = Math.max(Math.min(config.getInt("start-frame", 0), 999), 0);
		floorFrame = Math.max(Math.min(config.getInt("floor-frame", 999), 999), 0);
		ceilingFrame = Math.max(Math.min(config.getInt("ceiling-frame", 999), 999), 0);
		delay = Math.max(config.getInt("delay", 0), 0);

		fadeOut = config.getBoolean("fade-out", false);
		reverse = config.getBoolean("reverse", false);
	}

	@Override
	protected Runnable playEffectEntity(Entity entity, SpellData data) {
		if ((entity instanceof Player player)) new FontAnimation(player, fontNameSpace, fontName, titlePart, interval, durationTicks, startFrame, floorFrame, ceilingFrame, delay, fadeOut, reverse);

		return null;
	}

	private class FontAnimation implements Runnable {

		private String fontNameSpace;
		private String fontName;
		private String titlePart;

		private int durationTicks;
		private int floorFrame;
		private int ceilingFrame;

		private boolean fadeOut;
		private boolean reverse;

		private int fontAnimationTaskId;
		private int iterations;
		private int nextFrame;

		private Player target;

		private ArrayList<Character> list;

		private FontAnimation(Player target, String fontNameSpace, String fontName, String titlePart, int interval, int durationTicks, int startFrame, int floorFrame, int ceilingFrame, int delay, boolean fadeOut, boolean reverse) {
			this.target = target;

			this.fontNameSpace = fontNameSpace;
			this.fontName = fontName;
			this.titlePart = titlePart;

			this.durationTicks = durationTicks;
			this.floorFrame = floorFrame;
			this.ceilingFrame = ceilingFrame;
			this.iterations = 0;
			this.nextFrame = startFrame;

			this.fadeOut = fadeOut;
			this.reverse = reverse;

			if (!this.reverse) {
				this.nextFrame--;
			}

			this.list = new ArrayList<Character>();

			int from = 0;
			int until;

			if (this.ceilingFrame > this.floorFrame && this.ceilingFrame > this.nextFrame) {
				until = this.ceilingFrame;
			} else {
				until = 999;
			}

			until = ceilingFrame;
			while (from <= until)
				this.list.add(unescapeString(String.format("\\uE%03d", from++)).charAt(0));

			this.fontAnimationTaskId = MagicSpells.scheduleRepeatingTask(this, delay, interval);
		}

		@Override
		public void run() {
			if (this.iterations < this.durationTicks) {
				if (this.reverse) {
					if (this.nextFrame == this.floorFrame - 1) {
						this.nextFrame = this.ceilingFrame;
					} else if (this.nextFrame < 0) {
						this.nextFrame = 999;
					} else {
						this.nextFrame--;
					}
				} else {
					if (this.nextFrame == this.ceilingFrame + 1) {
						this.nextFrame = this.floorFrame;
					} else if (this.nextFrame > 999) {
						this.nextFrame = 0;
					} else {
						this.nextFrame++;
					}
				}

				Component message = Component.text(this.list.get(this.nextFrame) + "").font(Key.key(this.fontNameSpace, this.fontName));

				switch (this.titlePart) {
					case "actionbar":
						this.target.sendActionBar(message);
						break;
					default:
						this.target.showTitle(Title.title(Component.text(""), message, Title.Times.times(milisOfTicks(0), milisOfTicks(20), milisOfTicks(0))));
				}

				this.iterations++;
			} else {
				if (!this.fadeOut) {
					switch (this.titlePart) {
						case "actionbar":
							this.target.sendActionBar(Component.text(""));
							break;
						default:
							this.target.showTitle(Title.title(Component.text(""), Component.text(""), Title.Times.times(milisOfTicks(0), milisOfTicks(1), milisOfTicks(0))));
					}
				}
				MagicSpells.cancelTask(this.fontAnimationTaskId);
				return;
			}
		}

		private static Duration milisOfTicks(int ticks) {
			return Duration.ofMillis(TimeUtil.MILLISECONDS_PER_SECOND * (ticks / TimeUtil.TICKS_PER_SECOND));
		}
		
		//Functioned borrowed from AnimationCore
		private final static String unescapeString(String oldstr) {

			/*
			* In contrast to fixing Java's broken regex charclasses, this one need be no
			* bigger, as unescaping shrinks the string here, where in the other one, it
			* grows it.
			*/

			StringBuffer newstr = new StringBuffer(oldstr.length());

			boolean saw_backslash = false;

			for (int i = 0; i < oldstr.length(); i++) {
				int cp = oldstr.codePointAt(i);
				if (oldstr.codePointAt(i) > Character.MAX_VALUE) {
					i++; /**** WE HATES UTF-16! WE HATES IT FOREVERSES!!! ****/
				}

				if (!saw_backslash) {
					if (cp == '\\') {
						saw_backslash = true;
					} else {
						newstr.append(Character.toChars(cp));
					}
					continue; /* switch */
				}

				if (cp == '\\') {
					saw_backslash = false;
					newstr.append('\\');
					newstr.append('\\');
					continue; /* switch */
				}

				switch (cp) {

					case 'r':
						newstr.append('\r');
						break; /* switch */

					case 'n':
						newstr.append('\n');
						break; /* switch */

					case 'f':
						newstr.append('\f');
						break; /* switch */

					/* PASS a \b THROUGH!! */
					case 'b':
						newstr.append("\\b");
						break; /* switch */

					case 't':
						newstr.append('\t');
						break; /* switch */

					case 'a':
						newstr.append('\007');
						break; /* switch */

					case 'e':
						newstr.append('\033');
						break; /* switch */

					/*
					* A "control" character is what you get when you xor its codepoint with
					* '@'==64. This only makes sense for ASCII, and may not yield a "control"
					* character after all.
					*
					* Strange but true: "\c{" is ";", "\c}" is "=", etc.
					*/
					case 'c': {
						if (++i == oldstr.length()) {
							die("trailing \\c");
						}
						cp = oldstr.codePointAt(i);
						/*
						* don't need to grok surrogates, as next line blows them up
						*/
						if (cp > 0x7f) {
							die("expected ASCII after \\c");
						}
						newstr.append(Character.toChars(cp ^ 64));
						break; /* switch */
					}

					case '8':
					case '9':
						die("illegal octal digit");
						/* NOTREACHED */

						/*
						* may be 0 to 2 octal digits following this one so back up one for fallthrough
						* to next case; unread this digit and fall through to next case.
						*/
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
						--i;
						/* FALLTHROUGH */

						/*
						* Can have 0, 1, or 2 octal digits following a 0 this permits larger values
						* than octal 377, up to octal 777.
						*/
					case '0': {
						if (i + 1 == oldstr.length()) {
							/* found \0 at end of string */
							newstr.append(Character.toChars(0));
							break; /* switch */
						}
						i++;
						int digits = 0;
						int j;
						for (j = 0; j <= 2; j++) {
							if (i + j == oldstr.length()) {
								break; /* for */
							}
							/* safe because will unread surrogate */
							int ch = oldstr.charAt(i + j);
							if (ch < '0' || ch > '7') {
								break; /* for */
							}
							digits++;
						}
						if (digits == 0) {
							--i;
							newstr.append('\0');
							break; /* switch */
						}
						int value = 0;
						try {
							value = Integer.parseInt(oldstr.substring(i, i + digits), 8);
						} catch (NumberFormatException nfe) {
							die("invalid octal value for \\0 escape");
						}
						newstr.append(Character.toChars(value));
						i += digits - 1;
						break; /* switch */
					} /* end case '0' */

					case 'x': {
						if (i + 2 > oldstr.length()) {
							die("string too short for \\x escape");
						}
						i++;
						boolean saw_brace = false;
						if (oldstr.charAt(i) == '{') {
							/* ^^^^^^ ok to ignore surrogates here */
							i++;
							saw_brace = true;
						}
						int j;
						for (j = 0; j < 8; j++) {

							if (!saw_brace && j == 2) {
								break; /* for */
							}

							/*
							* ASCII test also catches surrogates
							*/
							int ch = oldstr.charAt(i + j);
							if (ch > 127) {
								die("illegal non-ASCII hex digit in \\x escape");
							}

							if (saw_brace && ch == '}') {
								break;
								/* for */ }

							if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'))) {
								die(String.format("illegal hex digit #%d '%c' in \\x", ch, ch));
							}

						}
						if (j == 0) {
							die("empty braces in \\x{} escape");
						}
						int value = 0;
						try {
							value = Integer.parseInt(oldstr.substring(i, i + j), 16);
						} catch (NumberFormatException nfe) {
							die("invalid hex value for \\x escape");
						}
						newstr.append(Character.toChars(value));
						if (saw_brace) {
							j++;
						}
						i += j - 1;
						break; /* switch */
					}

					case 'u': {
						if (i + 4 > oldstr.length()) {
							die("string too short for \\u escape");
						}
						i++;
						int j;
						for (j = 0; j < 4; j++) {
							/* this also handles the surrogate issue */
							if (oldstr.charAt(i + j) > 127) {
								die("illegal non-ASCII hex digit in \\u escape");
							}
						}
						int value = 0;
						try {
							value = Integer.parseInt(oldstr.substring(i, i + j), 16);
						} catch (NumberFormatException nfe) {
							die("invalid hex value for \\u escape");
						}
						newstr.append(Character.toChars(value));
						i += j - 1;
						break; /* switch */
					}

					case 'U': {
						if (i + 8 > oldstr.length()) {
							die("string too short for \\U escape");
						}
						i++;
						int j;
						for (j = 0; j < 8; j++) {
							/* this also handles the surrogate issue */
							if (oldstr.charAt(i + j) > 127) {
								die("illegal non-ASCII hex digit in \\U escape");
							}
						}
						int value = 0;
						try {
							value = Integer.parseInt(oldstr.substring(i, i + j), 16);
						} catch (NumberFormatException nfe) {
							die("invalid hex value for \\U escape");
						}
						newstr.append(Character.toChars(value));
						i += j - 1;
						break; /* switch */
					}

					default:
						newstr.append('\\');
						newstr.append(Character.toChars(cp));
						/*
						* say(String.format( "DEFAULT unrecognized escape %c passed through", cp));
						*/
						break; /* switch */

				}
				saw_backslash = false;
			}

			/* weird to leave one at the end */
			if (saw_backslash) {
				newstr.append('\\');
			}

			return newstr.toString();
		}

		/**
		 * Private util to crash with a useful message.
		 * 
		 * @param foa String to be printed in the exception
		 */
		private static final void die(String foa) {
			throw new IllegalArgumentException(foa);
		}
	}
}
