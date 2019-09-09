/*
 * Copyright 2018 Lane W. Surface
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

/**
 * Thrown when a given {@code Location} falls outside of a terminal's text-buffer.
 */
@SuppressWarnings("serial")
public class LocationOutOfBoundsException extends IndexOutOfBoundsException {
  public LocationOutOfBoundsException(Location location) {
    super(location + " is out of bounds.");
  }

  public LocationOutOfBoundsException(String msg) {
    super(msg);
  }
}