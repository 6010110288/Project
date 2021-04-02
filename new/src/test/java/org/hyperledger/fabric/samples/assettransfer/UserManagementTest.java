/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public final class UserManagementTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockUserResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> userList;

        MockUserResultsIterator() {
            super();

            userList = new ArrayList<KeyValue>();

            userList.add(new MockKeyValue("user1",
                    "{ \"userID\": \"user1\", \"name\": \"A\", \"permission\": \"11\",\"position\": \"Owner\", \"description\": \"KhoHong\" }"));
            userList.add(new MockKeyValue("user2",
                    "{ \"userID\": \"user2\", \"name\": \"B\", \"permission\": \"11\",\"position\": \"Doctor\", \"description\": \"HYHospital\" }"));
            userList.add(new MockKeyValue("user3",
                    "{ \"userID\": \"user3\", \"name\": \"C\", \"permission\": \"00\",\"position\": \"Doctor\", \"description\": \"HYHospital\" }"));
            userList.add(new MockKeyValue("user4",
                    "{ \"userID\": \"user4\", \"name\": \"D\", \"permission\": \"01\",\"position\": \"Doctor\", \"description\": \"HYHospital\" }"));
            userList.add(new MockKeyValue("user5",
                    "{ \"userID\": \"user5\", \"name\": \"E\", \"permission\": \"01\",\"position\": \"Nurse\", \"description\": \"HYHospital\" }"));
            userList.add(new MockKeyValue("user6",
                    "{ \"userID\": \"user6\", \"name\": \"F\", \"permission\": \"01\",\"position\": \"Caretaker\", \"description\": \"HYHospital\" }"));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return userList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        UserManagement contract = new UserManagement();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);

        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeReadUserTransaction {

        @Test
        public void whenUserExists() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1"))
                    .thenReturn("{ \"userID\": \"user1\", \"name\": \"A\", \"permission\": \"11\",\"position\": \"Owner\", \"description\": \"KhoHong\" }");

            User user = contract.GetUser(ctx, "user1");

            assertThat(user).isEqualTo(new User("user1", "A", "11", "Owner", "KhoHong"));
        }

        @Test
        public void whenUserDoesNotExist() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.GetUser(ctx, "user1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("User user1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("USER_NOT_FOUND".getBytes());
        }
    }

    @Test
    void invokeInitLedgerTransaction() {
        UserManagement contract = new UserManagement();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        contract.InitLedger(ctx);

        InOrder inOrder = inOrder(stub);
        inOrder.verify(stub).putStringState("user1", "{\"description\":\"KhoHong\",\"name\":\"Alice\",\"permission\":\"11\",\"position\":\"Owner\",\"userID\":\"user1\"}");
    }

    @Nested
    class InvokeAddNewUserTransaction {

        @Test
        public void whenUSerExists() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1"))
                    .thenReturn("{ \"userID\": \"user1\", \"name\": \"A\", \"permission\": \"11\",\"position\": \"Owner\", \"description\": \"KhoHong\" }");

            Throwable thrown = catchThrowable(() -> {
                contract.AddNewUser(ctx, "user1", "A", "11", "Owner", "KhoHong");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("User user1 already exists");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("USER_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenUserDoesNotExist() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1")).thenReturn("");

            User user = contract.AddNewUser(ctx, "user1", "A", "11", "Owner", "KhoHong");

            assertThat(user).isEqualTo(new User("user1", "A", "11", "Owner", "KhoHong"));
        }
    }

    @Test
    void invokeGetAllUsersTransaction() {
        UserManagement contract = new UserManagement();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStateByRange("", "")).thenReturn(new MockUserResultsIterator());

        String users = contract.GetAllUsers(ctx);

        assertThat(users).isEqualTo("[{\"description\":\"KhoHong\",\"name\":\"A\",\"permission\":\"11\",\"position\":\"Owner\",\"userID\":\"user1\"},"
                + "{\"description\":\"HYHospital\",\"name\":\"B\",\"permission\":\"11\",\"position\":\"Doctor\",\"userID\":\"user2\"},"
                + "{\"description\":\"HYHospital\",\"name\":\"C\",\"permission\":\"00\",\"position\":\"Doctor\",\"userID\":\"user3\"},"
                + "{\"description\":\"HYHospital\",\"name\":\"D\",\"permission\":\"01\",\"position\":\"Doctor\",\"userID\":\"user4\"},"
                + "{\"description\":\"HYHospital\",\"name\":\"E\",\"permission\":\"01\",\"position\":\"Nurse\",\"userID\":\"user5\"},"
                + "{\"description\":\"HYHospital\",\"name\":\"F\",\"permission\":\"01\",\"position\":\"Caretaker\",\"userID\":\"user6\"}]");

    }

    @Nested
    class UpdateUserTransaction {

        @Test
        public void whenUserExists() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1"))
                    .thenReturn("{ \"userID\": \"user1\", \"name\": \"A\", \"permission\": \"11\",\"position\": \"Owner\", \"description\": \"KhoHong\" }");

            User user = contract.UpdateUser(ctx, "user1", "00");

            assertThat(user).isEqualTo(new User("user1", "A", "00", "Owner", "KhoHong"));
        }

        @Test
        public void whenUserDoesNotExist() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateUser(ctx, "user1", "00");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("User user1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("USER_NOT_FOUND".getBytes());
        }
    }

    @Nested
    class DeleteUserTransaction {

        @Test
        public void whenUserDoesNotExist() {
            UserManagement contract = new UserManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("user1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.DeleteUser(ctx, "user1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("User user1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("USER_NOT_FOUND".getBytes());
        }
    }
}
