package com.toss.tosspaybackend.domain.member.entity;

import com.toss.tosspaybackend.domain.member.enums.Gender;
import com.toss.tosspaybackend.domain.member.enums.MobileCarrier;
import com.toss.tosspaybackend.domain.member.enums.Nationality;
import com.toss.tosspaybackend.global.basetime.AuditListener;
import com.toss.tosspaybackend.global.basetime.Auditable;
import com.toss.tosspaybackend.global.basetime.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditListener.class)
public class Member implements Auditable {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    private String residentRegistrationNumberFront;
    private String residentRegistrationNumberBack;

    @Enumerated(EnumType.STRING)
    private MobileCarrier mobileCarrier;
    private LocalDateTime birthdate;

    private String password;

    @Setter
    @Embedded
    @Column(nullable = false)
    private BaseTime baseTime;

    @Builder
    private Member(String name, String phone, Gender gender, String password, Nationality nationality,
                  String residentRegistrationNumberFront, String residentRegistrationNumberBack,
                  MobileCarrier mobileCarrier, LocalDateTime birthdate) {
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.password = password;
        this.nationality = nationality;
        this.residentRegistrationNumberFront = residentRegistrationNumberFront;
        this.residentRegistrationNumberBack = residentRegistrationNumberBack;
        this.mobileCarrier = mobileCarrier;
        this.birthdate = birthdate;
    }

    public static Member of(String name, String phone, Gender gender, String password, Nationality nationality,
                            String residentRegistrationNumberFront, String residentRegistrationNumberBack,
                            MobileCarrier mobileCarrier, LocalDateTime birthdate) {
        return Member.builder()
                .name(name)
                .phone(phone)
                .gender(gender)
                .password(password)
                .nationality(nationality)
                .residentRegistrationNumberFront(residentRegistrationNumberFront)
                .residentRegistrationNumberBack(residentRegistrationNumberBack)
                .mobileCarrier(mobileCarrier)
                .birthdate(birthdate)
                .build();
    }
}