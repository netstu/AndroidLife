/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.camnter.otto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps a 'producer' method on a specific object.
 * <p/>
 * <p> This class only verifies the suitability of the method and event type if something fails.
 * Callers are expected
 * to verify their uses of this class.
 *
 * @author Jake Wharton
 */
class EventProducer {

    /**
     * Object sporting the producer method.
     */
    final Object target;
    /**
     * Producer method.
     */
    private final Method method;
    /**
     * Object hash code.
     */
    private final int hashCode;
    /**
     * Should this producer produce events?
     * 标识该producer可否创建事件？
     */
    private boolean valid = true;

    EventProducer(Object target, Method method) {
        if (target == null) {
            throw new NullPointerException("EventProducer target cannot be null.");
        }
        if (method == null) {
            throw new NullPointerException("EventProducer method cannot be null.");
        }

        this.target = target;
        this.method = method;

        /**
         * 取消 Java语言访问检查，提高反射速度
         */
        method.setAccessible(true);

        /**
         * 计算hashCode
         */
        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        final int prime = 31;
        hashCode = (prime + method.hashCode()) * prime + target.hashCode();
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * If invalidated, will subsequently refuse to produce events.
     * <p/>
     * Should be called when the wrapped object is unregistered from the Bus.
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * Invokes the wrapped producer method.
     *
     * @throws java.lang.IllegalStateException             if previously invalidated.
     * @throws java.lang.reflect.InvocationTargetException if the wrapped method throws any {@link
     *                                                     Throwable} that is not
     *                                                     an {@link Error} ({@code Error}s are propagated as-is).
     */
    public Object produceEvent() throws InvocationTargetException {
        /**
         * 检查是否可以创建事件
         */
        if (!valid) {
            throw new IllegalStateException(
                    toString() + " has been invalidated and can no longer produce events.");
        }

        /**
         * 调用target类里的
         * method方法
         * 创建一个event
         *
         * 这里的
         * target = listener
         * method = @Produce方法
         */
        try {
            return method.invoke(target);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return "[EventProducer " + method + "]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        /**
         * 地址一样 视为相同
         */
        if (this == obj) {
            return true;
        }

        /**
         * obj 为 null
         * 想都不想为false
         */
        if (obj == null) {
            return false;
        }

        /**
         * 判断是否是一个Class
         */
        if (getClass() != obj.getClass()) {
            return false;
        }

        final EventProducer other = (EventProducer) obj;

        /**
         * 到这里，是地址不一样，就意味着hashcode不一样
         * 但是如果method 和 target相同的话
         * 那么视为相同的 EventProducer
         */
        return method.equals(other.method) && target == other.target;
    }
}
