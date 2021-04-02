/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class User {

    @Property()
    private final String userID;

    @Property()
    private final String name;

    @Property()
    private final String permission;

    @Property()
    private final String position;

    @Property()
    private final String description;

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public User(@JsonProperty("userID") final String userID, @JsonProperty("name") final String name,
            @JsonProperty("permission") final String permission, @JsonProperty("position") final String position,
            @JsonProperty("description") final String description) {
        this.userID = userID;
        this.name = name;
        this.permission = permission;
        this.position = position;
        this.description = description;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        User other = (User) obj;

        return Objects.deepEquals(
                new String[] {getUserID(), getName(), getPermission(), getPosition(), getDescription()},
                new String[] {other.getUserID(), other.getName(), other.getPermission(), other.getPosition(), other.getDescription()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserID(), getName(), getPermission(), getPosition(), getDescription());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [userID=" + userID + ", name="
                + name + ", permission=" + permission + ", position=" + position + ", description=" + description + "]";
    }
}
