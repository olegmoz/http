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
package com.artipie.http.auth;

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.headers.WwwAuthenticate;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * Slice with HTTP authentication.
 *
 * @since 0.17
 */
public final class AuthSlice implements Slice {

    /**
     * Origin.
     */
    private final Slice origin;

    /**
     * Authentication scheme.
     */
    private final AuthScheme auth;

    /**
     * Permissions.
     */
    private final Permission perm;

    /**
     * Ctor.
     *
     * @param origin Origin slice.
     * @param auth Authentication scheme.
     * @param perm Permissions.
     */
    public AuthSlice(final Slice origin, final AuthScheme auth, final Permission perm) {
        this.origin = origin;
        this.auth = auth;
        this.perm = perm;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        final Response response;
        if (this.perm.allowed(Permissions.ANY_USER)) {
            response = this.origin.response(line, headers, body);
        } else {
            response = new AsyncResponse(
                this.auth.authenticate(headers).thenApply(
                    result -> result.user().map(this.perm::allowed).map(
                        allowed -> {
                            final Response rsp;
                            if (allowed) {
                                rsp = this.origin.response(line, headers, body);
                            } else {
                                rsp = new RsWithStatus(RsStatus.FORBIDDEN);
                            }
                            return rsp;
                        }
                    ).orElseGet(
                        () -> new RsWithHeaders(
                            new RsWithStatus(RsStatus.UNAUTHORIZED),
                            new Headers.From(new WwwAuthenticate(result.challenge()))
                        )
                    )
                )
            );
        }
        return response;
    }
}
