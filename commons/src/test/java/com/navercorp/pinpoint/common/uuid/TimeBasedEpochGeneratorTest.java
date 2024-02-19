/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.uuid;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class TimeBasedEpochGeneratorTest {

    private static final int SZ_TEST = 10000;

    private final TimeBasedEpochGenerator generator = new TimeBasedEpochGenerator(null);

    @Test
    public void shouldMonotoneIncreasing() {
        UUID[] uuids = generateUuids();
        for (int i = 0; i < uuids.length - 1; i++) {
            if (uuids[i].compareTo(uuids[i + 1]) > 0) {
                throw new IllegalStateException("Not increasing");
            }
        }
    }

    @Test
    public void shouldUnique() {
        UUID[] uuids = generateUuids();
        Arrays.sort(uuids);
        for (int i = 0; i < uuids.length - 1; i++) {
            if (uuids[i].equals(uuids[i + 1])) {
                throw new IllegalStateException("Not unique");
            }
        }
    }

    private UUID[] generateUuids() {
        UUID[] uuids = new UUID[SZ_TEST];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = generator.generate();
        }
        return uuids;
    }

}
