/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class UserTest {

    @Nested
    class Equality {

        @Test
        public void isReflexive() {
            User user = new User("user1", "Alice", "11", "Owner", "KhoHong");

            assertThat(user).isEqualTo(user);
        }

        @Test
        public void isSymmetric() {
            User userA = new User("user1", "Alice", "11", "Owner", "KhoHong");
            User userB = new User("user1", "Alice", "11", "Owner", "KhoHong");

            assertThat(userA).isEqualTo(userB);
            assertThat(userB).isEqualTo(userA);
        }

        @Test
        public void isTransitive() {
            User userA = new User("user1", "Alice", "11", "Owner", "KhoHong");
            User userB = new User("user1", "Alice", "11", "Owner", "KhoHong");
            User userC = new User("user1", "Alice", "11", "Owner", "KhoHong");

            assertThat(userA).isEqualTo(userB);
            assertThat(userB).isEqualTo(userC);
            assertThat(userA).isEqualTo(userC);
        }

        @Test
        public void handlesInequality() {
            User userA = new User("user1", "Alice", "11", "Owner", "KhoHong");
            User userB = new User("user2", "Bob", "11", "Doctor", "HYHospital");

            assertThat(userA).isNotEqualTo(userB);
        }

        @Test
        public void handlesOtherObjects() {
            User userA = new User("user1", "Alice", "11", "Owner", "KhoHong");
            String userB = "not a user";

            assertThat(userA).isNotEqualTo(userB);
        }

        @Test
        public void handlesNull() {
            User user = new User("user1", "Alice", "11", "Owner", "KhoHong");

            assertThat(user).isNotEqualTo(null);
        }
    }

    @Test
    public void toStringIdentifiesUser() {
        User user = new User("user1", "Alice", "11", "Owner", "KhoHong");

        assertThat(user.toString()).isEqualTo("User@421e95a4 [userID=user1, name=Alice, permission=11, position=Owner, description=KhoHong]");
    }
}
