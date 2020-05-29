/*
 * Copyright 2019-2020 Elypia CIC
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

package org.elypia.retropia.core.requests;

import org.elypia.retropia.core.exceptions.FriendlyException;
import org.slf4j.*;
import retrofit2.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @param <T> The type of response this will return.
 * @author seth@elypia.org (Seth Falco)
 */
public class RestAction<T> implements RestInterface<T> {

    private static final Logger logger = LoggerFactory.getLogger(RestAction.class);

    protected Call<T> call;
    protected List<Consumer<T>> pipes;

    public RestAction(Call<T> call) {
        this.call = call;
        this.pipes = new ArrayList<>();

        logger.debug("Created new RestAction with url: {}", call.request().url());
    }

    @Override
    public void queue(Consumer<T> success, Consumer<Throwable> ex) {
        logger.debug("Queued RestAction with url: {}", call.request().url());

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (!response.isSuccessful()) {
                    logger.warn("HTTP request failed with status code {}.", response.code());
                    onFailure(call, new HttpException(response));
                }

                T body = response.body();

                for (Consumer<T> pipe : pipes) {
                    try {
                        pipe.accept(body);
                    } catch (FriendlyException ex) {
                        onFailure(call, ex);
                        return;
                    }
                }

                if (success != null)
                    success.accept(body);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (ex != null)
                    ex.accept(t);
            }
        });
    }

    @Override
    public T complete() throws IOException {
        logger.debug("Called Complete on RestAction with url: {}", call.request().url());
        Response<T> response = call.execute();

        if (!response.isSuccessful()) {
            logger.warn("HTTP request failed with status code {}.", response.code());
            throw new HttpException(response);
        }

        T body = response.body();

        for (Consumer<T> pipe : pipes)
            pipe.accept(body);

        return body;
    }

    @Override
    public void cancel() {
        call.cancel();
    }

    /**
     * Add a post action to perform or process the result prior to
     * performing the success action.
     *
     * @param consumer The action to perform.
     */
    public void pipe(Consumer<T> consumer) {
        pipes.add(consumer);
    }
}
