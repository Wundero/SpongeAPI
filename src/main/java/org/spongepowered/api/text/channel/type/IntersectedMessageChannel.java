/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.api.text.channel.type;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A message channel that targets all subjects contained within the given channels
 * and applies the message transformations of each channel in order (so with n
 * channels,
 * {@code channels[n-1].transformMessage(channels[n-2].transformMessage(channels[...]
 * .transformMessage(channels[0].transformMessage(input))))} would occur)
 */
public class IntersectedMessageChannel implements MessageChannel {

    protected final Collection<MessageChannel> channels;

    public IntersectedMessageChannel(MessageChannel... channels) {
        this(Arrays.asList(channels));
    }

    public IntersectedMessageChannel(Collection<MessageChannel> channels) {
        this.channels = ImmutableSet.copyOf(channels);
    }

    @Override
    public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
        Text text = original;
        for (MessageChannel channel : this.channels) {
            text = channel.transformMessage(sender, recipient, text, type).orElse(text);
        }

        return Optional.ofNullable(text);
    }

    @Override
    public Collection<MessageReceiver> getMembers() {
        // Collects members present in all channels. (Intersecting members).
        // Not sure how to do this with streams.
        List<MessageChannel> channels = new ArrayList<>();
        channels.addAll(this.channels);
        if (channels.size() == 1) {
            return channels.get(0).getMembers();
        }
        List<MessageReceiver> sr = new ArrayList<>();
        List<MessageReceiver> tr = new ArrayList<>();
        sr.addAll(channels.get(0).getMembers());
        for (int i = 1; i < channels.size(); i++) {
            for (MessageReceiver r : sr) {
                if (!channels.get(i).getMembers().contains(r)) {
                    tr.add(r);
                }
            }
            for (MessageReceiver r : tr) {
                sr.remove(r);
            }
            tr.clear();
        }
        return sr;
    }

}
