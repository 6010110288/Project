/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "userman",
        info = @Info(
                title = "User management",
                description = "The hyperlegendary user management",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "6010110288@psu.ac.th",
                        name = "M.Yingsung",
                        url = "-")))
@Default
public final class UserManagement implements ContractInterface {

    private final Genson genson = new Genson();

    private enum UserManagementErrors {
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        PERMISSION_DENIED
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        AddNewUser(ctx, "user1", "user1", "11", "Owner", "KhoHong");
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User AddNewUser(final Context ctx, final String userID, final String name, final String permission,
        final String position, final String description) {
        ChaincodeStub stub = ctx.getStub();
        ClientIdentity ci = ctx.getClientIdentity();

        if (UserExists(ctx, userID)) {
            String errorMessage = String.format("User %s already exists", userID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.USER_ALREADY_EXISTS.toString());
        }
        if (!ci.getAttributeValue("username").equals(stub.getChannelId())) {
            String errorMessage = String.format("Pemission denied for user %s", name);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.PERMISSION_DENIED.toString());
        }

        User user = new User(userID, name, permission, position, description);
        String userJSON = genson.serialize(user);
        stub.putStringState(userID, userJSON);

        return user;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public User GetUser(final Context ctx, final String userID) {
        ChaincodeStub stub = ctx.getStub();
        String userJSON = stub.getStringState(userID);

        if (userJSON == null || userJSON.isEmpty()) {
            String errorMessage = String.format("User %s does not exist", userID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.USER_NOT_FOUND.toString());
        }

        User user = genson.deserialize(userJSON, User.class);
        return user;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User UpdateUser(final Context ctx, final String userID, final String newPermission) {
        ChaincodeStub stub = ctx.getStub();
        ClientIdentity ci = ctx.getClientIdentity();
        String userJSON = stub.getStringState(userID);

        if (userJSON == null || userJSON.isEmpty()) {
            String errorMessage = String.format("User %s does not exist", userID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.USER_NOT_FOUND.toString());
        }

        User user = genson.deserialize(userJSON, User.class);

        if (!ci.getAttributeValue("username").equals(stub.getChannelId())) {
            String errorMessage = String.format("Pemission denied for user %s", user.getName());
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.PERMISSION_DENIED.toString());
        }

        User newUser = new User(user.getUserID(), user.getName(), newPermission, user.getPosition(), user.getDescription());
        String newUserJSON = genson.serialize(newUser);
        stub.putStringState(userID, newUserJSON);

        return newUser;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteUser(final Context ctx, final String userID) {
        ChaincodeStub stub = ctx.getStub();
        ClientIdentity ci = ctx.getClientIdentity();
        String userJSON = stub.getStringState(userID);

        if (!UserExists(ctx, userID)) {
            String errorMessage = String.format("User %s does not exist", userID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.USER_NOT_FOUND.toString());
        }

        User user = genson.deserialize(userJSON, User.class);

        if (!ci.getAttributeValue("username").equals(stub.getChannelId())) {
            String errorMessage = String.format("Pemission denied for user %s", user.getName());
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserManagementErrors.PERMISSION_DENIED.toString());
        }
        stub.delState(userID);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean UserExists(final Context ctx, final String userID) {
        ChaincodeStub stub = ctx.getStub();
        String userJSON = stub.getStringState(userID);

        return (userJSON != null && !userJSON.isEmpty());
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllUsers(final Context ctx, final String userID) {
        ChaincodeStub stub = ctx.getStub();
        ClientIdentity ci = ctx.getClientIdentity();
        String userJSON = stub.getStringState(userID);

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        User user = genson.deserialize(userJSON, User.class);

        return user.getPermission();
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetUserPermission(final Context ctx, final String userID) {
        ChaincodeStub stub = ctx.getStub();
        ClientIdentity ci = ctx.getClientIdentity();
        String userJSON = stub.getStringState(userID);

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        User user = genson.deserialize(userJSON, User.class);

        return user.getPermission();
    }
}
