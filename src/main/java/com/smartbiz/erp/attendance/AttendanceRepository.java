package com.smartbiz.erp.attendance;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    java.util.Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    java.util.List<Attendance> findAllByEmployeeIdInAndWorkDateBetween(java.util.List<Long> employeeIds, LocalDate start, LocalDate end);

    java.util.List<Attendance> findAllByEmployeeIdInAndWorkDate(java.util.List<Long> employeeIds, LocalDate workDate);

    @Query("""
        select new com.smartbiz.erp.attendance.AttendanceListView(
		    a.attendanceId,
		    a.employeeId,
		    e.name,
		    a.workDate,
		    a.checkInTime,
		    a.checkOutTime,
		    a.status,
		    a.overtimeMinutes,
		    a.note
		)
		from Attendance a
		left join Employee e
		       on e.employeeId = a.employeeId
		where (:workDate is null or a.workDate = :workDate)
		  and (:status is null or a.status = :status)
		  and (
		        :keyword is null or :keyword = '' or
		        (:searchField = 'id' and concat('', a.employeeId) like concat('%', :keyword, '%')) or
		        (:searchField <> 'id' and e.name like concat('%', :keyword, '%'))
		  )
		order by a.workDate desc, a.attendanceId desc
    """)
    Page<AttendanceListView> searchPage(
            @Param("searchField") String searchField,
            @Param("keyword") String keyword,
            @Param("workDate") LocalDate workDate,
            @Param("status") Attendance.Status status,
            Pageable pageable
    );
}
