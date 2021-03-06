/*
 * Copyright 2018, 2019 Lane W. Surface
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jtxt.emulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A string of glyphs, with various methods to aid in the conversion of an escaped
 * {@code String} into individual character elements and persisted in an instance of
 * this class. This is effectively a replacement for strings, where each character
 * has color information embedded with it.
 *
 * @see Glyph
 */
public final class GString implements Iterable<Glyph> {
  /**
   * The glyphs that this string contains. As this class mimics String itself, it's
   * meant to be immutable; the size and values stored in this array should never
   * change once initialized.
   */
  private final Glyph[] glyphs;

  /**
   * Constructs a new GString from the array of glyphs.
   *
   * @param glyphs The glyphs this string should contain.
   */
  public GString(Glyph[] glyphs) {
    this.glyphs = glyphs;
  }

  /**
   * Returns the length of this string of glyphs. Note that if this object was
   * created by the GString#of(String) method, the length of this object may not
   * necessarily be the same length as the given string. See this method for more
   * information about how glyphs are constructed from their respective string
   * representation.
   *
   * @return The length of this string of glyphs.
   */
  public int length() {
    return glyphs.length;
  }

  /**
   * Gets the glyph in this string at the given index. This method is analagous to
   * {@link String#charAt(int) charAt}.
   *
   * @param index The index within this string to use.
   *
   * @return The glyph in this string at the given index.
   */
  public Glyph get(int index) {
    return glyphs[index];
  }

  /**
   * Appends the given string to this one. Glyphs belonging to each string retain
   * their respective properties during this process. Color information may differ
   * between the two.
   *
   * @param other The string to append to this one.
   *
   * @return A new GString containing the elements of this string followed by the
   *   elements of the other, as if they were simply appended to the end of the
   *   array. (As color information is extracted during the conversion from a string
   *   to glyphs, escapes in one will not affect the other.)
   */
  public GString concat(GString other) {
    int thisLength = length(),
      otherLength = other.length();

    Glyph[] glyphs = new Glyph[thisLength + otherLength];
    System.arraycopy(
      this.glyphs,
      0,
      glyphs,
      0,
      thisLength);
    System.arraycopy(
      other.glyphs,
      0,
      glyphs,
      thisLength,
      otherLength);

    return new GString(glyphs);
  }

  /**
   * Inserts a new glyph into this string at the index.
   *
   * @param index The index at which to insert the given glyph into the string.
   * @param glyph The glyph to insert.
   *
   * @return A new string containing the glyph inserted at the index. The glyph at
   *   the index, as well as all glyphs after it will have been shifted to the right
   *   to account for the inserted glyph.
   */
  public GString insert(
    int index,
    Glyph glyph)
  {
    Glyph[] glyphs = new Glyph[this.glyphs.length+1];
    System.arraycopy(
      this.glyphs,
      0,
      glyphs,
      0,
      index);
    glyphs[index] = glyph;
    System.arraycopy(
      this.glyphs,
      index,
      glyphs,
      index + 1,
      this.glyphs.length-index);

    return new GString(glyphs);
  }

  /**
   * Appends the given glyph to the end of this string. This is equivalent to
   * concatenating this string with a string containing the given glyph, assuming
   * that the string to be appended has a length of one.
   *
   * @param glyph The glyph to append to the end of this string.
   *
   * @return A new string containing this string and the given glyph inserted at the
   *   end, with a length of one + the length of this string.
   */
  public GString append(Glyph glyph) {
    Glyph[] glyphs = new Glyph[this.glyphs.length+1];
    glyphs[glyphs.length-1] = glyph;
    System.arraycopy(
      this.glyphs,
      0,
      glyphs,
      0,
      this.glyphs.length);

    return new GString(glyphs);
  }

  /**
   * Sets the glyph in this string at the index to the given glyph, discarding the
   * glyph that was at that position within the string previously.
   *
   * @param index The position within this string to change the glyph of.
   * @param glyph The glyph that will replace the glyph which occupied this
   *   position previously.
   *
   * @return A new string with the given glyph at the index.
   */
  public GString set(
    int index,
    Glyph glyph)
  {
    Glyph[] glyphs = Arrays.copyOf(
      this.glyphs,
      this.glyphs.length);
    glyphs[index] = glyph;

    return new GString(glyphs);
  }

  public GString substring(
    int start,
    int end)
  {
    return new GString(Arrays.copyOfRange(
      glyphs,
      start,
      end));
  }

  /**
   * Converts this string into a series of ANSI escape sequences so that it can be
   * printed at a console. Color information is preserved.
   *
   * @param start The first character within this {@code GString} that should be
   *   escaped into a String.
   * @param end The last character in this GString that should be escaped.
   *
   * @return A substring of this {@code GString} that has color information embedded
   *   within it in the form of ANSI escape sequences.
   */
  public String getData(
    int start,
    int end)
  {
    StringBuilder data = new StringBuilder();

    for (int i = start; i < end; i++) {
      Glyph glyph = glyphs[i];
      Color fg,
        bg;

      fg = glyph.color;
      bg = glyph.background;
      // TODO: Construct ANSI escapes with an ASNIEscapeBuilder class.
      data.append(String.format(
        "\u001B[38;2;%d;%d;%dm"
        + "\u001B[48;2;%d;%d;%dm",
        /* Foreground components: */
        fg.getRed(),
        fg.getGreen(),
        fg.getBlue(),
        /* Background components: */
        bg.getRed(),
        bg.getGreen(),
        bg.getBlue()));
      data.append(glyph.character).append("\u001B[0m");
    }

    return data.toString();
  }

  @Override
  public String toString() {
    int[] data = Arrays.stream(glyphs)
      .mapToInt(g -> g.character)
      .toArray();

    return getStringFromArray(data);
  }

  private String getStringFromArray(int[] string) {
    char[] chars = new char[string.length];
    for (int i = 0; i < chars.length; i++) {
      if (string[i] == '\0') {
        chars[i] = ' ';
        continue;
      }
      chars[i] = (char)string[i];
    }

    return new String(chars);
  }

  /**
   * Creates a new string with blank {@code Glyphs}. This string is guaranteed to not
   * appear in the terminal when rendered.
   *
   * @param length The number of null characters that the string should contain.
   *
   * @return A new {@code GString} containing all null Glyphs and has the length
   *   specified.
   *
   * @see Glyph#BLANK
   */
  public static GString blank(int length) {
    Glyph[] glyphs = new Glyph[length];
    Arrays.fill(
      glyphs,
      Glyph.BLANK);

    return new GString(glyphs);
  }

  /**
   * Constructs a {@code GString} for the given string of text. The presence of
   * escape sequences in the string (delimited by <code>\e[...m</code>) is taken into
   * account when constructing these glyphs; therefore, do not necessarily expect
   * that the length of the given text and the length of the string returned by this
   * method will be the same.
   *
   * @param text The string of text to convert to glyphs.
   * @param background The color which will appear behind the text. Use {@link
   *   Glyph#TRANSPARENT} if you wish to retain the default color of the terminal.
   *   (This is essentially a highlight color.)
   *
   * @return A GString with escaped values extracted and applied to their respective
   *   glyphs.
   */
  public static GString of(
    String text,
    Color background)
  {
    /*
     * Support extracting color data in the form of
     * "\e[<0..255>;<0..255>;<0..255>m".
     */
    ArrayList<Glyph> glyphs = new ArrayList<>();
    Color current = Color.WHITE;

    int index = 0;
    while (index < text.length()) {
      if (text.startsWith(
        "\\e",
        index))
      {
        index += 3; // Advance past `\e[`.
        String[] colorData = text.substring(
          index,
          text.indexOf('m'))
          .split(";");

        int[] components = new int[colorData.length];
        for (int ci = 0; ci < components.length; ci++) {
          String component = colorData[ci];
          components[ci] = Integer.parseInt(component);
          index += component.length();
        }

        current = new Color(
          components[0],
          components[1],
          components[2]);
        index++; // Account for `m` character.
      }

      glyphs.add(new Glyph(
        text.charAt(index),
        current,
        background));
      index++;
    }

    return new GString(glyphs.toArray(new Glyph[0]));
  }

  /**
   * Wraps this string to the given <code>length</code>. This method (rather
   * primitively) breaks the string on the spaces between individual words.
   *
   * <p>
   * <i>Implementation Note</i>: This algorithm is greedy and makes no
   * attempt to balance the distribution of {@code Glyphs} between lines.
   * </p>
   *
   * @param length The maximum number of Glyphs that can appear on a line before
   *   being wrapped.
   *
   * @return An array of {@code GString}s, where each line's length is guaranteed to
   *   be no greater than the given length, and spaces between words at the rightmost
   *   bound of a line are discarded.
   */
  public GString[] wrap(int length) {
    if (glyphs.length > length) {
      /*
       * Keeps track of the index of the first character in the line that
       * still needs to be wrapped.
       */
      int index = 0;

      /*
       * The number of characters that still need to be wrapped.
       */
      int delta = glyphs.length;

      ArrayList<GString> lines = new ArrayList<>();

      while (delta > length) {
        for (int i = index + length; i > index; i--) {
          if (glyphs[i].character == ' ') {
            lines.add(substring(
              index,
              i));
            delta -= ++i - index;
            index = i;

            break;
          }
        }
      }
      lines.add(substring(
        index,
        glyphs.length));

      return lines.toArray(new GString[0]);
    }

    return new GString[] { this };
  }

  public Iterator<Glyph> iterator() {
    return new GlyphIterator(this);
  }

  private static class GlyphIterator implements Iterator<Glyph> {
    /**
     * The index of the next glyph to return.
     */
    private int index;

    /**
     * A reference to the {@code GString} we are iterating over.
     */
    private GString string;

    /**
     * Create an iterator for the given string.
     *
     * @param string The string of glyphs to iterate over.
     */
    public GlyphIterator(GString string) {
      this.string = string;
    }

    public boolean hasNext() {
      return index < string.length();
    }

    public Glyph next() {
      return string.get(index++);
    }
  }
}
