/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.stream;

import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ByteByByteSplit}.
 *
 * @since 0.4
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ByteByByteSplitTest {

    @Test
    public void basicSplitWorks() {
        final ByteByByteSplit split = new ByteByByteSplit(" ".getBytes());
        this.buffersOfOneByteFlow("how are you").subscribe(split);
        MatcherAssert.assertThat(
            new ByteFlowAsString(split).value(),
            new IsEqual<>("howareyou")
        );
    }

    @Test
    public void severalCharSplitWorks() {
        final ByteByByteSplit split = new ByteByByteSplit("__".getBytes());
        this.buffersOfOneByteFlow("how__are__you").subscribe(split);
        MatcherAssert.assertThat(
            new ByteFlowAsString(split).value(),
            new IsEqual<>("howareyou")
        );
    }

    private Flowable<ByteBuffer> buffersOfOneByteFlow(final String str) {
        return Flowable.fromArray(
            Arrays.stream(
                ArrayUtils.toObject(str.getBytes())
            ).map(
                (Byte aByte) -> {
                    final byte[] bytes = new byte[1];
                    bytes[0] = aByte;
                    return ByteBuffer.wrap(bytes);
                }
            ).toArray(ByteBuffer[]::new)
        );
    }
}
