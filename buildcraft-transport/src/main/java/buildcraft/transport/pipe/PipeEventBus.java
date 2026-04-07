/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPriority;

/**
 * Simplified event bus for pipe events. Scans registered handler objects for methods annotated
 * with {@link PipeEventHandler} and dispatches {@link PipeEvent} instances to matching handlers.
 */
public class PipeEventBus {
    private static final Map<Class<?>, List<HandlerEntry>> HANDLER_CACHE = new HashMap<>();
    private final List<RegisteredHandler> handlers = new ArrayList<>();

    /** Registers an object whose methods annotated with {@link PipeEventHandler} will receive events. */
    public void registerHandler(Object handler) {
        if (handler == null) return;
        List<HandlerEntry> entries = getHandlerEntries(handler.getClass());
        for (HandlerEntry entry : entries) {
            handlers.add(new RegisteredHandler(handler, entry));
        }
        handlers.sort(Comparator.comparingInt(h -> h.entry.priority.ordinal()));
    }

    /** Unregisters all handler methods from the given object. */
    public void unregisterHandler(Object handler) {
        if (handler == null) return;
        handlers.removeIf(h -> h.instance == handler);
    }

    /** Fire an event to all registered handlers whose parameter type matches. */
    public boolean fireEvent(PipeEvent event) {
        if (event == null) return false;
        boolean handled = false;
        for (RegisteredHandler handler : handlers) {
            if (event.isCanceled() && !handler.entry.receiveCancelled) {
                continue;
            }
            if (handler.entry.eventType.isInstance(event)) {
                try {
                    handler.entry.handle.invoke(handler.instance, event);
                    handled = true;
                } catch (Throwable t) {
                    BCLog.logger.error("Failed to invoke pipe event handler {} for event {}",
                        handler.entry.method, event.getClass().getSimpleName(), t);
                }
            }
        }
        String error = event.checkStateForErrors();
        if (error != null) {
            BCLog.logger.warn("Pipe event {} had state errors: {}", event.getClass().getSimpleName(), error);
        }
        return handled;
    }

    private static List<HandlerEntry> getHandlerEntries(Class<?> clazz) {
        return HANDLER_CACHE.computeIfAbsent(clazz, PipeEventBus::scanClass);
    }

    private static List<HandlerEntry> scanClass(Class<?> clazz) {
        List<HandlerEntry> entries = new ArrayList<>();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (Method method : clazz.getMethods()) {
            PipeEventHandler annotation = method.getAnnotation(PipeEventHandler.class);
            if (annotation == null) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !PipeEvent.class.isAssignableFrom(params[0])) {
                BCLog.logger.warn("@PipeEventHandler method {} has wrong signature, expected single PipeEvent parameter",
                    method);
                continue;
            }
            try {
                MethodHandle handle = lookup.unreflect(method);
                entries.add(new HandlerEntry(method, handle, params[0].asSubclass(PipeEvent.class),
                    annotation.priority(), annotation.receiveCancelled()));
            } catch (IllegalAccessException e) {
                BCLog.logger.error("Failed to create MethodHandle for {}", method, e);
            }
        }
        return entries;
    }

    private record HandlerEntry(
        Method method,
        MethodHandle handle,
        Class<? extends PipeEvent> eventType,
        PipeEventPriority priority,
        boolean receiveCancelled
    ) {}

    private record RegisteredHandler(Object instance, HandlerEntry entry) {}
}
