package ru.nsu.server.model.potential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Subject;
import ru.nsu.server.model.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "potential_friday")
@NoArgsConstructor
@Data
public class PotentialFriday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "subject_name")
//    private Subject subjectName;
//
//    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinTable(name = "group_subjects",
//            joinColumns = @JoinColumn(name = "time_table_id"),
//            inverseJoinColumns = @JoinColumn(name = "group_id"))
//    @ToString.Exclude
//    @JsonIgnore
//    private List<Group> groups = new ArrayList<>();
//
//    @OneToOne
//    @JoinColumn(name = "user_id")
//    @JsonIgnore
//    @ToString.Exclude
//    private User teacher;
//
//    @JsonFormat(timezone = "Asia/Novosibirsk")
//    @Column(name = "start_time")
//    private Date startTime;
//
//    @Column(name = "room")
//    private String room;
//
//    @JsonFormat(timezone = "Asia/Novosibirsk")
//    @Column(name = "end_time")
//    private Date endTime;
//
//    @Column(name = "pair_type")
//    private String pairType;
}
