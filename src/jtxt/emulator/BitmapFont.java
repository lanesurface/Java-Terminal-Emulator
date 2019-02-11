/* 
 * Copyright 2019 Lane W. Surface
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
import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * A font which has been rasterized and saved in an appropriate image format.
 * This kind of font does not need to be rasterized but, rather, only needs to
 * serve a matching glyph from the image when requested to do so.
 * 
 * @see #getCharacterAsImage(Glyph)
 */
public class BitmapFont {
    /**
     * The ASCII code for the first character in the bitmap font. This defines
     * the starting point for the range of character values that this font
     * supports.
     */
    public final int minCodePoint;
    
    /**
     * The ASCII code for the last character in the bitmap font. This defines
     * the ending point for the range of character values that this font
     * supports.
     */
    public final int maxCodePoint;
    
    protected int charWidth,
                  charHeight;
    
    private int xOffset,
                yOffset;
    
    /**
     * The raw glyph values within this font. (Each of these rasters must
     * be processed before it can be appropriately rendered.)
     */
    private WritableRaster[] glyphs;
    
    private ColorModel inCM,
                       outCM;
    
    /**
     * The color that will be filtered out of the bitmap. All pixels of this
     * color will be turned transparent.
     */
    private int colorMask = 0x000000;
    
    protected BitmapFont(Path fontPath,
                         int charWidth,
                         int charHeight,
                         int minCodePoint,
                         int numPoints) {
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        this.minCodePoint = minCodePoint;
        maxCodePoint = minCodePoint + numPoints;
        glyphs = new WritableRaster[numPoints];
        
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nbits = { 8, 8, 8 };
        inCM = new ComponentColorModel(cs,
                                       nbits,
                                       false, /* hasAlpha */
                                       false, /* isAlphaPremultiplied */
                                       Transparency.OPAQUE,
                                       DataBuffer.TYPE_BYTE);
        outCM = new ComponentColorModel(cs,
                                        new int[] { 8, 8, 8, 8 },
                                        true,
                                        false,
                                        Transparency.BITMASK,
                                        DataBuffer.TYPE_BYTE);

        try {
            BufferedImage fontImage = ImageIO.read(fontPath.toFile());
            WritableRaster fontRaster = fontImage.getRaster();
            
            int cells = (int)Math.sqrt(numPoints);
            xOffset = fontImage.getWidth() / cells - charWidth;
            yOffset = fontImage.getHeight() / cells - charHeight;
            
            for (int r = 0; r < cells; r++) {
                for (int c = 0; c < cells; c++) {
                    int x = (charWidth + xOffset) * c,
                        y = (charHeight + yOffset) * r;
                    glyphs[r * cells + c] =
                        fontRaster.createWritableChild(x,
                                                       y,
                                                       charWidth,
                                                       charHeight,
                                                       0,
                                                       0,
                                                       null);
                }
            }
        }
        catch (IOException ie) { /* TODO */ }
    }
    
    public Image getCharacterAsImage(Glyph glyph) {
        char character = glyph.character;
        
        if (character == '\0') return null;
        if (character < minCodePoint || character > maxCodePoint) {
            System.out.format("The given character %c is outside the valid "
                              + "range of characters: [%d, %d].%n",
                              character,
                              minCodePoint,
                              maxCodePoint);
            
            
            character = '?';
        }
        
        WritableRaster raster = 
            transformGlyphToColor(glyphs[character - minCodePoint],
                                  glyph.color);
        
        return new BufferedImage(outCM,
                                 raster,
                                 outCM.isAlphaPremultiplied(),
                                 null);
    }
    
    protected WritableRaster transformGlyphToColor(WritableRaster raster,
                                                   Color color) {
        WritableRaster modified = 
            outCM.createCompatibleWritableRaster(raster.getWidth(),
                                                 raster.getHeight());
        
        for (int y = 0; y < charHeight; y++) {
            for (int x = 0; x < charWidth; x++) {
                int pix = inCM.getRGB(raster.getDataElements(x,
                                                             y,
                                                             null));
                boolean transparent = (pix << 8 | colorMask) == colorMask;
                pix &= color.getRGB();
                
                byte[] samples = new byte[outCM.getNumComponents()];
                inCM.getDataElements(pix, samples);
                samples[samples.length - 1] = transparent
                                              ? (byte)0
                                              : (byte)255;
                
                modified.setDataElements(x,
                                         y,
                                         samples);
            }
        }
        
        
        return modified;
    }
}
