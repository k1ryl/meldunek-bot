package k1ryl.meldunekbot.meldunek;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table
@Getter
@Setter
@EqualsAndHashCode
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long tgUserId;

    private String name;

    private String surname;

    private String pesel;

    private String countryOfBirth;

    private LocalDate dateOfBirth;

    private String placeOfBirth;

    private String countryOfResidence;

    private String phone;

    private String email;

    private String street;

    private String houseNumber;

    private String flatNumber;

    private String postalCode;

    private String cityOrCityDistrict;

    private String gmina;

    private String voivodeship;

    private LocalDate periodOfResidenceFrom;

    private LocalDate periodOfResidenceTo;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)")
    private ApplicationState state;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private String stateDetails;

    @Column(updatable = false, nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}