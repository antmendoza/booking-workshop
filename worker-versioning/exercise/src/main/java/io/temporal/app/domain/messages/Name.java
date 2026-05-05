package io.temporal.app.domain.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Name {

    String firstName = new String();
    String lastName = new String();
    String preferedLanguage = "en";

    public Name() {}

    public Name(String fullName) {
        var parts = (fullName == null ? "" : fullName.trim())
                .split("\\s+", 2);
        this.firstName = parts[0].isEmpty() ? null : parts[0];
        this.lastName = parts.length == 2 ? parts[1] : null;
    }

    @JsonIgnore
    public String getName() {
        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
