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

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.swing.JComponent;

public class Renderer extends JComponent {
    private static final long serialVersionUID = 1L;

    /**
     * This is a screenshot of the screen before we began rendering. When the
     * transparency of the window is less than <code>1.0f</code>, the pixels of
     * this image are combined with those of the terminal, to give an illusion
     * of a transparent window.
     */
    private final BufferedImage screen;
    
    /**
     * The color that is painted behind the text in the terminal.
     */
    private Color background;
    
    /**
     * The transparency of this component. (A value less than <code>1.0f</code>
     * will cause the values of pixels behind the window to be combined with
     * those of the terminal, to give the illusion of transparency.)
     */
    private float transparency;
    
    /**
     * The {@code GlyphRasterizer} that is used to convert the {@code Glyphs}
     * in the {@code currentFrame} into an image.
     */
    private GlyphRasterizer rasterizer;
    
    /**
     * The frame that is being painted to the window, where the last call to
     * this render generated this image. This rasterized image may be painted
     * to the window as many times as necessary before the next frame is
     * requested.
     * 
     * @see Renderer#renderFrame(BufferedFrame)
     */
    private RenderedImage rasterizedFrame;
    
    /**
     * The kind of {@code GlyphRasterizer} that should be created when we
     * construct an instance of the {@code Renderer}.
     */
    public static enum RasterType { HARDWARE_ACCELERATED,
                                    SOFTWARE }
    
    protected Renderer(Color background,
                       float transparency,
                       GlyphRasterizer rasterizer) {
        if (transparency != 1.0f) {
            BufferedImage scr = null;
            try {
                Robot r = new Robot();
                Toolkit tk = Toolkit.getDefaultToolkit();
                Dimension bounds = tk.getScreenSize();
                scr = r.createScreenCapture(new Rectangle(0,
                                                          0,
                                                          bounds.width,
                                                          bounds.height));
            } catch (AWTException awtex) { }
            this.screen = scr;
        }
        else screen = null;
        
        this.background = background;
        this.transparency = transparency;
        this.rasterizer = rasterizer;
    }
    
    public static Renderer getInstance(Context context,
                                       Color background,
                                       float transparency,
                                       RasterType rasterType) {
        switch (rasterType) {
        case HARDWARE_ACCELERATED:
            /*
             * TODO: This is planned in the future; for now, use the software
             *       rasterizer instead.
             */
        case SOFTWARE:
            return new Renderer(background,
                                transparency,
                                new SWRasterizer(context));
        default:
            throw new IllegalArgumentException("The given raster type is " +
                                               "not recognized.");
        }
    }
    
    public void renderFrame(BufferedFrame frame) {
        rasterizedFrame = rasterizer.rasterize(frame);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        Graphics2D graphics = (Graphics2D)g;
        int width = getWidth(),
            height = getHeight();
        
        Point location = getLocationOnScreen();
        int startX = location.x,
            startY = location.y,
            endX = startX + width,
            endY = startY + height;
        /*
         * Draw the screenshot within the bounds of this renderer. If this
         * component is opaque, the image won't be visible anyway.
         */
        graphics.drawImage(screen,
                           0,
                           0,
                           width,
                           height,
                           startX,
                           startY,
                           endX,
                           endY,
                           null);
        
        Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                    transparency);
        graphics.setComposite(comp);
        graphics.setColor(background);
        graphics.fillRect(0,
                          0,
                          width,
                          height);
        
        if (rasterizedFrame == null) return;
        graphics.drawRenderedImage(rasterizedFrame, null);
    }
}