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
package jtxt.emulator.tui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jtxt.GlyphBuffer;
import jtxt.emulator.GString;
import jtxt.emulator.Glyph;
import jtxt.emulator.Location;
import jtxt.emulator.Region;

/**
 * A {@code Container} is a Component which owns and manages other Components. The
 * Components which a Container manages are said to be <i>children</i> of that
 * Container. Various elements of a Container may be specified when it's created,
 * including the {@code Layout} which defines how these children will be allocated
 * bounds within their parent.
 *
 * @param <T> The type of {@code Components} this Container should hold. In
 *   general, a Container may be instantiated with {@code Component}, but
 *   instantiation of subclass of {@code Component} is necessary if you wish for
 *   methods such as {@link #getChild(int)} to return Components of that type.
 */
public class Container<T extends Component>
  extends Component
  implements Iterable<Component>
{
  /**
   * A collection of all the children this container owns. Components owned by this
   * container inherit certain properties of it. This container may also dictate the
   * way that components added to it appear on the screen.
   */
  protected List<T> children;

  /**
   * The layout that determines how the children of this container will be placed
   * within it.
   */
  protected Layout layout;

  @SafeVarargs
  public Container(
    Object parameters,
    Layout layout,
    T... children)
  {
    this.parameters = parameters;
    this.layout = layout;
    this.children = new ArrayList<>();

    add(children);
  }

  @SafeVarargs
  public Container(
    Object parameters,
    Layout layout,
    Color background,
    T... children)
  {
    this(
      parameters,
      layout,
      children);
    this.background = background;
  }

  /**
   * Adds the component to this container, using the inflated properties of that
   * component to determine the bounds it may occupy within this container.
   *
   * @param children The components to add to this container.
   */
  @SuppressWarnings("unchecked")
  public void add(T... children) {
    for (T child : children) {
      this.children.add(child);
      layout.setComponentBounds(child);
      child.setBackground(background);

      for (ComponentObserver co : observers)
        child.registerObserver(co);
    }

    update();
  }

  T getChild(int index) {
    return children.get(index);
  }

  public Component getComponentAt(Location location) {
    for (Component child : children) {
      if (location.inside(child.getBounds())) {
        if (child instanceof Container)
          return ((Container<?>)child).getComponentAt(location);

        return child;
      }
    }

    return location.inside(bounds)
      ? this
      : null;
  }

  /**
   * Returns the components in this container in the order defined by the layout that
   * has been set.
   *
   * @return The components that this container owns in the order defined by this
   *   container's layout.
   */
  public Component[] getChildren() {
    return children.toArray(new Component[0]);
  }

  @Override
  public Iterator<Component> iterator() {
    return new ContainerIterator();
  }

  private class ContainerIterator implements Iterator<Component> {
    /**
     * All of the children that are owned by this container, including any
     * sub-containers that it may contain.
     */
    private final Component[] children;

    /**
     * The index of the component in the array that is to be returned next.
     */
    private int index;

    /**
     * Construct an iterator for this container, where that container is the parent
     * of all components returned by this iterator.
     */
    private ContainerIterator() {
      children = getChildren();
    }

    @Override
    public boolean hasNext() {
      return index < children.length;
    }

    @Override
    public Component next() {
      /*
       * NOTE: This iterator returns Objects of type `Component`, not the
       *       parameterized type. This is because subcontainers may
       *       contain Components which are incompatible with the
       *       parameterized one.
       */
      Component current = children[index++];
      if (current instanceof Container) {
        Container<?> container = (Container<?>)current;
        for (Component component : container)
          return component;
      }

      return current;
    }
  }

  @Override
  public void draw(GlyphBuffer buffer) {
    Glyph background = new Glyph(
      '\u2588',
      this.background,
      Glyph.TRANSPARENT);
    Glyph[] glyphs = new Glyph[width];
    Arrays.fill(
      glyphs,
      background);
    GString string = new GString(glyphs);

    for (int l = bounds.start.line; l < bounds.end.line; l++) {
      buffer.update(string, Location.at(
        bounds,
        l,
        bounds.start.position));
    }

    for (Component child : children)
      child.draw(buffer);
  }

  @Override
  public void setBounds(Region bounds) {
    super.setBounds(bounds);

    layout.setParentBounds(bounds);
    children.stream().forEach(layout::setComponentBounds);
  }
}
