/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.instrument.mock.*;
import com.navercorp.pinpoint.profiler.interceptor.factory.ExceptionHandlerFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.TestInterceptorRegistryBinder;
import org.junit.jupiter.api.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ASMMethodNodeAdapterAddInterceptorTest {
    private final static InterceptorRegistryBinder interceptorRegistryBinder = new TestInterceptorRegistryBinder();
    private ASMClassNodeLoader.TestClassLoader classLoader;
    private AtomicInteger interceptorIdCounter = new AtomicInteger();

    private ExceptionHandlerFactory exceptionHandlerFactory = new ExceptionHandlerFactory(false);

    @BeforeAll
    public static void beforeClass() {
        interceptorRegistryBinder.bind();
        InterceptorRegistry.setInterceptorHolderEnable(true);
    }

    @AfterAll
    public static void afterClass() {
        interceptorRegistryBinder.unbind();
    }

    @BeforeEach
    public void before() {
        this.classLoader = ASMClassNodeLoader.getClassLoader();
    }

    @Test
    public void addArgsArrayInterceptor() throws Exception {
        addInterceptor(new ArgsArrayInterceptor());
    }

    @Test
    public void addStaticInterceptor() throws Exception {
        addInterceptor(new StaticInterceptor());
    }

    @Test
    public void addApiIdAwareInterceptor() throws Exception {
        addInterceptor(new ApiIdAwareInterceptor());
    }

    @Test
    public void addBasicInterceptor() throws Exception {
        addInterceptor(new BasicInterceptor());
    }

    @Disabled
    @Test
    public void addExceptionInterceptor() throws Exception {
        ExceptionHandler exceptionHandler = exceptionHandlerFactory.getExceptionHandler();
        ExceptionHandleAroundInterceptor interceptor = new ExceptionHandleAroundInterceptor(new ExceptionInterceptor(), exceptionHandler);
        addInterceptor(interceptor);
    }

    private void addInterceptor(Interceptor interceptor) throws Exception {
        // method
        checkMethod(interceptor);

        // constructor
        checkConstructor(interceptor);

        // arguments
        checkArguments(interceptor);

        // return
        checkReturn(interceptor);

        // exception
        checkMethodException(interceptor);
        checkConstructorException(interceptor);

        // extend
        checkExtends(interceptor);
    }

    private void checkMethod(Interceptor interceptor) throws Exception {
        // method
        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.MethodClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();
        Class<?>[] parameterTypes = new Class[0];
        Object[] args = new Object[0];
        invokeMethod(clazz, "publicMethod", interceptorClass, parameterTypes, args, null, false);
        invokeMethod(clazz, "publicStaticMethod", interceptorClass, parameterTypes, args, null, false);
        invokeMethod(clazz, "publicFinalMethod", interceptorClass, parameterTypes, args, null, false);
        invokeMethod(clazz, "publicStaticFinalMethod", interceptorClass, parameterTypes, args, null, false);
        invokeMethod(clazz, "publicSynchronizedMethod", interceptorClass, parameterTypes, args, null, false);
        invokeMethod(clazz, "publicStaticSynchronizedMethod", interceptorClass, parameterTypes, args, null, false);
        invokeMethod(clazz, "publicStaticFinalSynchronizedMethod", interceptorClass, parameterTypes, args, null, false);
    }

    private void checkConstructor(Interceptor interceptor) throws Exception {
        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ConstructorClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();
        invokeMethod(clazz, "<init>", interceptorClass, new Class[0], new Object[0], null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{byte.class}, new Object[]{Byte.parseByte("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{short.class}, new Object[]{Short.parseShort("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{int.class}, new Object[]{Integer.parseInt("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{float.class}, new Object[]{Float.parseFloat("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{double.class}, new Object[]{Double.parseDouble("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{boolean.class}, new Object[]{Boolean.parseBoolean("true")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{char.class}, new Object[]{Character.forDigit(0, 0)}, null, false);

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{byte[].class}, new Object[]{new byte[]{Byte.parseByte("0"), Byte.parseByte("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{short[].class}, new Object[]{new short[]{Short.parseShort("0"), Short.parseShort("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{int[].class}, new Object[]{new int[]{Integer.parseInt("0"), Integer.parseInt("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{float[].class}, new Object[]{new float[]{Float.parseFloat("0"), Float.parseFloat("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{double[].class}, new Object[]{new double[]{Double.parseDouble("0"), Double.parseDouble("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{boolean[].class}, new Object[]{new boolean[]{Boolean.parseBoolean("true"), Boolean.parseBoolean("false")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{char[].class}, new Object[]{new char[]{Character.forDigit(0, 0), Character.forDigit(1, 1)}}, null, false);

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{byte[][].class}, new Object[]{new byte[][]{{Byte.parseByte("0"), Byte.parseByte("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{short[][].class}, new Object[]{new short[][]{{Short.parseShort("0"), Short.parseShort("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{int[][].class}, new Object[]{new int[][]{{Integer.parseInt("0"), Integer.parseInt("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{float[][].class}, new Object[]{new float[][]{{Float.parseFloat("0"), Float.parseFloat("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{double[][].class}, new Object[]{new double[][]{{Double.parseDouble("0"), Double.parseDouble("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{boolean[][].class}, new Object[]{new boolean[][]{{Boolean.parseBoolean("true"), Boolean.parseBoolean("false")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{char[][].class}, new Object[]{new char[][]{{Character.forDigit(0, 0), Character.forDigit(1, 1)}}}, null, false);

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String.class}, new Object[]{"foo"}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Object.class}, new Object[]{new Object()}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Byte.class}, new Object[]{new Byte("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Short.class}, new Object[]{new Short("0")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Integer.class}, new Object[]{new Integer(0)}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Long.class}, new Object[]{new Long(0)}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Float.class}, new Object[]{new Float(0)}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Double.class}, new Object[]{new Double(0)}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Boolean.class}, new Object[]{new Boolean("true")}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Character.class}, new Object[]{new Character('0')}, null, false);

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String[].class}, new Object[]{new String[]{"foo", "bar"}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Object[].class}, new Object[]{new Object[]{new Object(), new Object()}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Byte[].class}, new Object[]{new Byte[]{new Byte("0"), new Byte("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Short[].class}, new Object[]{new Short[]{new Short("0"), new Short("1")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Integer[].class}, new Object[]{new Integer[]{new Integer(0), new Integer(1)}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Long[].class}, new Object[]{new Long[]{new Long(0), new Long(1)}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Float[].class}, new Object[]{new Float[]{new Float(0), new Float(1)}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Double[].class}, new Object[]{new Double[]{new Double(0), new Double(1)}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Boolean[].class}, new Object[]{new Boolean[]{new Boolean("true"), new Boolean("false")}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Character[].class}, new Object[]{new Character[]{new Character('0'), new Character('1')}}, null, false);

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String[][].class}, new Object[]{new String[][]{{"foo", "bar"}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Object[][].class}, new Object[]{new Object[][]{{new Object(), new Object()}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Byte[][].class}, new Object[]{new Byte[][]{{new Byte("0"), new Byte("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Short[][].class}, new Object[]{new Short[][]{{new Short("0"), new Short("1")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Integer[][].class}, new Object[]{new Integer[][]{{new Integer(0), new Integer(1)}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Long[][].class}, new Object[]{new Long[][]{{new Long(0), new Long(1)}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Float[][].class}, new Object[]{new Float[][]{{new Float(0), new Float(1)}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Double[][].class}, new Object[]{new Double[][]{{new Double(0), new Double(1)}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Boolean[][].class}, new Object[]{new Boolean[][]{{new Boolean("true"), new Boolean("false")}}}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Character[][].class}, new Object[]{new Character[][]{{new Character('0'), new Character('1')}}}, null, false);

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Class.class, Method.class, Field.class}, new Object[]{null, null, null}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String.class, int.class, byte.class, Object.class, Enum.class, char.class, float.class, long.class}, new Object[]{"foo", 1, Byte.parseByte("0"), new Object(), BaseEnum.AGENT, 'a', 1.1f, 1l}, null, false);
    }

    private void checkArguments(Interceptor interceptor) throws Exception {
        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();

        invokeMethod(clazz, "arg", interceptorClass, new Class[0], new Object[0], null, false);
        invokeMethod(clazz, "argByteType", interceptorClass, new Class[]{byte.class}, new Object[]{Byte.parseByte("0")}, null, false);
        invokeMethod(clazz, "argShortType", interceptorClass, new Class[]{short.class}, new Object[]{Short.parseShort("0")}, null, false);
        invokeMethod(clazz, "argIntType", interceptorClass, new Class[]{int.class}, new Object[]{Integer.parseInt("0")}, null, false);
        invokeMethod(clazz, "argFloatType", interceptorClass, new Class[]{float.class}, new Object[]{Float.parseFloat("0")}, null, false);
        invokeMethod(clazz, "argDoubleType", interceptorClass, new Class[]{double.class}, new Object[]{Double.parseDouble("0")}, null, false);
        invokeMethod(clazz, "argBooleanType", interceptorClass, new Class[]{boolean.class}, new Object[]{Boolean.parseBoolean("true")}, null, false);
        invokeMethod(clazz, "argCharType", interceptorClass, new Class[]{char.class}, new Object[]{Character.forDigit(0, 0)}, null, false);

        invokeMethod(clazz, "argByteArrayType", interceptorClass, new Class[]{byte[].class}, new Object[]{new byte[]{Byte.parseByte("0"), Byte.parseByte("1")}}, null, false);
        invokeMethod(clazz, "argShortArrayType", interceptorClass, new Class[]{short[].class}, new Object[]{new short[]{Short.parseShort("0"), Short.parseShort("1")}}, null, false);
        invokeMethod(clazz, "argIntArrayType", interceptorClass, new Class[]{int[].class}, new Object[]{new int[]{Integer.parseInt("0"), Integer.parseInt("1")}}, null, false);
        invokeMethod(clazz, "argFloatArrayType", interceptorClass, new Class[]{float[].class}, new Object[]{new float[]{Float.parseFloat("0"), Float.parseFloat("1")}}, null, false);
        invokeMethod(clazz, "argDoubleArrayType", interceptorClass, new Class[]{double[].class}, new Object[]{new double[]{Double.parseDouble("0"), Double.parseDouble("1")}}, null, false);
        invokeMethod(clazz, "argBooleanArrayType", interceptorClass, new Class[]{boolean[].class}, new Object[]{new boolean[]{Boolean.parseBoolean("true"), Boolean.parseBoolean("false")}}, null, false);
        invokeMethod(clazz, "argCharArrayType", interceptorClass, new Class[]{char[].class}, new Object[]{new char[]{Character.forDigit(0, 0), Character.forDigit(1, 1)}}, null, false);

        invokeMethod(clazz, "argByteArraysType", interceptorClass, new Class[]{byte[][].class}, new Object[]{new byte[][]{{Byte.parseByte("0"), Byte.parseByte("1")}}}, null, false);
        invokeMethod(clazz, "argShortArraysType", interceptorClass, new Class[]{short[][].class}, new Object[]{new short[][]{{Short.parseShort("0"), Short.parseShort("1")}}}, null, false);
        invokeMethod(clazz, "argIntArraysType", interceptorClass, new Class[]{int[][].class}, new Object[]{new int[][]{{Integer.parseInt("0"), Integer.parseInt("1")}}}, null, false);
        invokeMethod(clazz, "argFloatArraysType", interceptorClass, new Class[]{float[][].class}, new Object[]{new float[][]{{Float.parseFloat("0"), Float.parseFloat("1")}}}, null, false);
        invokeMethod(clazz, "argDoubleArraysType", interceptorClass, new Class[]{double[][].class}, new Object[]{new double[][]{{Double.parseDouble("0"), Double.parseDouble("1")}}}, null, false);
        invokeMethod(clazz, "argBooleanArraysType", interceptorClass, new Class[]{boolean[][].class}, new Object[]{new boolean[][]{{Boolean.parseBoolean("true"), Boolean.parseBoolean("false")}}}, null, false);
        invokeMethod(clazz, "argCharArraysType", interceptorClass, new Class[]{char[][].class}, new Object[]{new char[][]{{Character.forDigit(0, 0), Character.forDigit(1, 1)}}}, null, false);

        invokeMethod(clazz, "argArgs", interceptorClass, new Class[]{Object[].class}, new Object[]{new Object[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}}, null, false);
        invokeMethod(clazz, "argArgs2", interceptorClass, new Class[]{int.class, Object[].class}, new Object[]{1, new Object[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}}, null, false);
        invokeMethod(clazz, "argInt3", interceptorClass, new Class[]{int.class, int.class, int.class}, new Object[]{1, 2, 3}, null, false);
        invokeMethod(clazz, "argObject4", interceptorClass, new Class[]{String.class, Integer.class, Long.class, Float.class}, new Object[]{"foo", 1, 2l, 3.0f}, null, false);
        invokeMethod(clazz, "argString5", interceptorClass, new Class[]{String.class, String.class, String.class, String.class, String.class}, new Object[]{"0", "1", "2", "3", "4"}, null, false);
        invokeMethod(clazz, "argEnum", interceptorClass, new Class[]{Enum.class}, new Object[]{BaseEnum.AGENT}, null, false);
        invokeMethod(clazz, "argInterface", interceptorClass, new Class[]{Map.class, Map.class, Map.class}, new Object[]{new HashMap(), new HashMap<String, String>(), new HashMap<Object, Object>()}, null, false);
    }

    private void checkReturn(Interceptor interceptor) throws Exception {
        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ReturnClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();

        invokeMethod(clazz, "voidType", interceptorClass, new Class[0], new Object[0], null, false);
        invokeMethod(clazz, "returnByte", interceptorClass, new Class[0], new Object[0], Byte.parseByte("0"), false);
        invokeMethod(clazz, "returnByteObject", interceptorClass, new Class[0], new Object[0], Byte.valueOf("1"), false);
        invokeMethod(clazz, "returnInt", interceptorClass, new Class[0], new Object[0], Integer.parseInt("1"), false);
        invokeMethod(clazz, "returnIntObject", interceptorClass, new Class[0], new Object[0], Integer.valueOf("1"), false);
        invokeMethod(clazz, "returnFloat", interceptorClass, new Class[0], new Object[0], Float.parseFloat("1.1"), false);
        invokeMethod(clazz, "returnFloatObject", interceptorClass, new Class[0], new Object[0], Float.valueOf("1.1"), false);
        invokeMethod(clazz, "returnBoolean", interceptorClass, new Class[0], new Object[0], Boolean.parseBoolean("true"), false);
        invokeMethod(clazz, "returnBooleanObject", interceptorClass, new Class[0], new Object[0], Boolean.valueOf("true"), false);
        invokeMethod(clazz, "returnChar", interceptorClass, new Class[0], new Object[0], Character.forDigit(1, 1), false);
        invokeMethod(clazz, "returnCharObject", interceptorClass, new Class[0], new Object[0], Character.valueOf('1'), false);
        invokeMethod(clazz, "returnString", interceptorClass, new Class[0], new Object[0], new String("s"), false);
        invokeMethod(clazz, "returnEnum", interceptorClass, new Class[0], new Object[0], BaseEnum.AGENT, false);
    }

    private void checkMethodException(Interceptor interceptor) throws Exception {
        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ExceptionClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();

        invokeMethod(clazz, "throwable", interceptorClass, new Class[]{}, new Object[]{}, null, true);
        invokeMethod(clazz, "exception", interceptorClass, new Class[]{}, new Object[]{}, null, true);
        invokeMethod(clazz, "runtime", interceptorClass, new Class[]{}, new Object[]{}, null, true);
        invokeMethod(clazz, "io", interceptorClass, new Class[]{}, new Object[]{}, null, true);
        invokeMethod(clazz, "io2", interceptorClass, new Class[]{}, new Object[]{}, null, false);
        invokeMethod(clazz, "condition", interceptorClass, new Class[]{}, new Object[]{}, null, true);
    }

    private void checkConstructorException(Interceptor interceptor) throws Exception {
        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ConstructorExceptionClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();

        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String.class, int.class}, new Object[]{"foo", 0}, null, true);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Boolean.class}, new Object[]{Boolean.TRUE}, null, true);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Boolean.class}, new Object[]{Boolean.FALSE}, null, false);
    }

    private void checkExtends(Interceptor interceptor) throws Exception {
        addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ConstructorParentClass", interceptor);
        Class<?> interceptorClass = interceptor.getClass();

        Class<?> clazz = addInterceptor0("com.navercorp.pinpoint.profiler.instrument.mock.ConstructorChildClass", interceptor);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{}, new Object[]{}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String.class}, new Object[]{"foo"}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{String.class, int.class}, new Object[]{"foo", 1}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{Object.class}, new Object[]{"foo"}, null, false);
        invokeMethod(clazz, "<init>", interceptorClass, new Class[]{int.class}, new Object[]{1}, null, false);
    }

    private void invokeMethod(final Class<?> clazz, final String methodName, final Class<?> interceptorClass, final Class<?>[] parameterTypes, final Object[] args, final Object returnValue, final boolean throwable) throws Exception {
        ArgsArrayInterceptor.clear();
        StaticInterceptor.clear();
        ApiIdAwareInterceptor.clear();
        BasicInterceptor.clear();
        ExceptionInterceptor.clear();

        Constructor<?> constructor;
        Method method = null;
        if (methodName.equals("<init>")) {
            constructor = clazz.getConstructor(parameterTypes);
            try {
                constructor.newInstance(args);
                if (throwable) {
                    fail("can't throw Throwable.");
                }
            } catch (Throwable t) {
                if (!throwable) {
                    throw new RuntimeException(t.getMessage(), t);
                }
            }
        } else {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
            try {
                method.invoke(clazz.newInstance(), args);
                if (throwable) {
                    fail("can't throw Throwable.");
                }
            } catch (Throwable t) {
                if (!throwable) {
                    throw new RuntimeException(t.getMessage(), t);
                }
            }
        }

        final String name = clazz.getName() + "." + methodName;
        if (interceptorClass == ArgsArrayInterceptor.class) {
            assertTrue(ArgsArrayInterceptor.before, name);
            assertTrue(ArgsArrayInterceptor.after, name);

            if (method != null && Modifier.isStatic(method.getModifiers())) {
                assertNull(ArgsArrayInterceptor.beforeTarget, name);
                assertNull(ArgsArrayInterceptor.afterTarget, name);
            } else if (method != null) {
                assertNotNull(ArgsArrayInterceptor.beforeTarget, name);
                assertNotNull(ArgsArrayInterceptor.afterTarget, name);
            }
            assertEquals(ArgsArrayInterceptor.beforeTarget, ArgsArrayInterceptor.afterTarget, name);

            if (ArgsArrayInterceptor.beforeArgs != null) {
                assertThat(args).as(name).containsExactly(ArgsArrayInterceptor.beforeArgs);
            }

            if (ArgsArrayInterceptor.afterArgs != null) {
                assertThat(args).as(name).containsExactly(ArgsArrayInterceptor.afterArgs);
            }
            assertEquals(returnValue, ArgsArrayInterceptor.result, name);
            if (throwable) {
                assertNotNull(ArgsArrayInterceptor.throwable, name);
            }
        } else if (interceptorClass == ExceptionInterceptor.class) {
            assertTrue(ExceptionInterceptor.before, name);
            assertTrue(ExceptionInterceptor.after, name);

            if (method != null && Modifier.isStatic(method.getModifiers())) {
                assertNull(ExceptionInterceptor.beforeTarget, name);
                assertNull(ExceptionInterceptor.afterTarget, name);
            } else if (method != null) {
                assertNotNull(ExceptionInterceptor.beforeTarget, name);
                assertNotNull(ExceptionInterceptor.afterTarget, name);
            }
            assertEquals(ExceptionInterceptor.beforeTarget, ExceptionInterceptor.afterTarget, name);

            if (ExceptionInterceptor.beforeArgs != null) {
                assertThat(args).as(name).containsExactly(ExceptionInterceptor.beforeArgs);
            }

            if (ExceptionInterceptor.afterArgs != null) {
                assertThat(args).as(name).containsExactly(ExceptionInterceptor.afterArgs);
            }

            assertEquals(returnValue, ExceptionInterceptor.result, name);
            if (throwable) {
                assertNotNull(ExceptionInterceptor.throwable, name);
            }
        } else if (interceptorClass == StaticInterceptor.class) {
            assertTrue(StaticInterceptor.before);
            assertTrue(StaticInterceptor.after);

            if (method != null && Modifier.isStatic(method.getModifiers())) {
                assertNull(StaticInterceptor.beforeTarget, name);
                assertNull(StaticInterceptor.afterTarget, name);
            } else if (method != null) {
                assertNotNull(StaticInterceptor.beforeTarget, name);
                assertNotNull(StaticInterceptor.afterTarget, name);
            }
            assertEquals(StaticInterceptor.beforeTarget, StaticInterceptor.afterTarget);

            assertEquals(clazz.getName(), StaticInterceptor.beforeClassName);
            assertNotNull(StaticInterceptor.beforeMethodName);
            assertNotNull(StaticInterceptor.beforeParameterDescription);
            assertNotNull(StaticInterceptor.afterClassName);
            assertNotNull(StaticInterceptor.afterMethodName);
            assertNotNull(StaticInterceptor.afterParameterDescription);

            if (StaticInterceptor.beforeArgs != null) {
                assertThat(args).as(name).containsExactly(StaticInterceptor.beforeArgs);
            }

            if (StaticInterceptor.afterArgs != null) {
                assertThat(args).as(name).containsExactly(StaticInterceptor.afterArgs);
            }

            assertEquals(returnValue, StaticInterceptor.result);
            if (throwable) {
                assertNotNull(StaticInterceptor.throwable, name);
            }
        } else if (interceptorClass == ApiIdAwareInterceptor.class) {
            assertTrue(ApiIdAwareInterceptor.before, name);
            assertTrue(ApiIdAwareInterceptor.after, name);

            if (method != null && Modifier.isStatic(method.getModifiers())) {
                assertNull(ApiIdAwareInterceptor.beforeTarget, name);
                assertNull(ApiIdAwareInterceptor.afterTarget, name);
            } else if (method != null) {
                assertNotNull(ApiIdAwareInterceptor.beforeTarget, name);
                assertNotNull(ApiIdAwareInterceptor.afterTarget, name);
            }
            assertEquals(ApiIdAwareInterceptor.beforeTarget, ApiIdAwareInterceptor.afterTarget, name);

            assertEquals(99, ApiIdAwareInterceptor.beforeApiId);
            assertEquals(99, ApiIdAwareInterceptor.afterApiId);

            if (ApiIdAwareInterceptor.beforeArgs != null) {
                assertThat(args).as(name).containsExactly(ApiIdAwareInterceptor.beforeArgs);
            }

            if (ApiIdAwareInterceptor.afterArgs != null) {
                assertThat(args).as(name).containsExactly(ApiIdAwareInterceptor.afterArgs);
            }

            assertEquals(returnValue, ApiIdAwareInterceptor.result, name);
            if (throwable) {
                assertNotNull(ApiIdAwareInterceptor.throwable, name);
            }
        } else if (interceptorClass == BasicInterceptor.class) {
            assertTrue(BasicInterceptor.before, name);
            assertTrue(BasicInterceptor.after, name);

            if (method != null && Modifier.isStatic(method.getModifiers())) {
                assertNull(BasicInterceptor.beforeTarget, name);
                assertNull(BasicInterceptor.afterTarget, name);
            } else if (method != null) {
                assertNotNull(BasicInterceptor.beforeTarget, name);
                assertNotNull(BasicInterceptor.afterTarget, name);
            }
            assertEquals(BasicInterceptor.beforeTarget, BasicInterceptor.afterTarget, name);


            if (args != null && args.length >= 1) {
                assertEquals(args[0], BasicInterceptor.beforeArg0);
                assertEquals(args[0], BasicInterceptor.afterArg0);
            }
            if (args != null && args.length >= 2) {
                assertEquals(args[1], BasicInterceptor.beforeArg1);
                assertEquals(args[1], BasicInterceptor.afterArg1);
            }

            if (args != null && args.length >= 3) {
                assertEquals(args[2], BasicInterceptor.beforeArg2);
                assertEquals(args[2], BasicInterceptor.afterArg2);
            }

            if (args != null && args.length >= 4) {
                assertEquals(args[3], BasicInterceptor.beforeArg3);
                assertEquals(args[3], BasicInterceptor.afterArg3);
            }

            if (args != null && args.length >= 5) {
                assertEquals(args[4], BasicInterceptor.beforeArg4);
                assertEquals(args[4], BasicInterceptor.afterArg4);
            }

            assertEquals(returnValue, BasicInterceptor.result, name);
            if (throwable) {
                assertNotNull(BasicInterceptor.throwable, name);
            }
        }
    }

    private Class<?> addInterceptor0(final String targetClassName, Interceptor interceptor) {
        final InterceptorDefinition interceptorDefinition = new InterceptorDefinitionFactory().createInterceptorDefinition(interceptor.getClass());
        try {
            classLoader.setTrace(false);
            classLoader.setVerify(false);
            classLoader.setTargetClassName(targetClassName);
            classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
                @Override
                public void handle(ClassNode classNode) {
                    List<MethodNode> methodNodes = classNode.methods;
                    for (MethodNode methodNode : methodNodes) {
                        if (methodNode.name.equals("<clinit>")) {
                            continue;
                        }

                        ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
                        if (methodNodeAdapter.isAbstract() || methodNodeAdapter.isNative()) {
                            continue;
                        }
                        int interceptorId = interceptorIdCounter.incrementAndGet();
                        try {
                            ASMInterceptorHolder.create(interceptorId, classLoader, interceptor);
                        } catch (InstrumentException e) {
                            throw new RuntimeException(e);
                        }
                        methodNodeAdapter.addBeforeInterceptor(interceptorId, interceptorDefinition, 99);
                        methodNodeAdapter.addAfterInterceptor(interceptorId, interceptorDefinition, 99);
                    }
                }
            });
            return classLoader.loadClass(targetClassName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}