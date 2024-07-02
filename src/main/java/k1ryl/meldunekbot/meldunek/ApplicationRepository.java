package k1ryl.meldunekbot.meldunek;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    void deleteAllByTgUserId(Long tgUserId);

    Optional<Application> findByTgUserIdOrderByUpdatedAtDesc(Long tgUserId);
}
