/*
 * Copyright 2019 Lane W. Surface
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package jtxt;

public class Console extends Terminal {
    public Console() {
        super(80, 20); // FIXME: At best, we can only guess right now.
        surface = createDrawableSurface(width,
                                        height);
    }

    @Override
    protected DrawableSurface createDrawableSurface(int width, int height) {
        return new ANSIWriter(System.out /*,
                              width,
                              height */);
    }
}
