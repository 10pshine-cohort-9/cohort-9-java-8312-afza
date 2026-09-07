package com.contactmanager.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.contactmanager.backend.dto.RegistrationRequest;
import com.contactmanager.backend.repository.UserRepository;

@SpringBootTest
class ConcurrentUserRegistrationIntegrationTests {

    @Autowired
    private UserRegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void concurrentRegistrationsForSameIdentifierBothReturnAccepted() throws Exception {
        String email = "concurrent-" + UUID.randomUUID() + "@example.com";
        RegistrationRequest request = new RegistrationRequest(
                "Concurrent", "User", email, null, "valid-password");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<RegistrationResult> first = executor.submit(() -> registerWhenReleased(request, ready, start));
            Future<RegistrationResult> second = executor.submit(() -> registerWhenReleased(request, ready, start));

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            RegistrationResult firstResult = first.get(30, TimeUnit.SECONDS);
            RegistrationResult secondResult = second.get(30, TimeUnit.SECONDS);

            assertThat(firstResult.response()).isEqualTo(secondResult.response());
            assertThat(firstResult.created() ^ secondResult.created()).isTrue();
            assertThat(userRepository.findAllByIdentifier(email)).hasSize(1);
        } finally {
            userRepository.deleteAll(userRepository.findAllByIdentifier(email));
        }
    }

    private RegistrationResult registerWhenReleased(RegistrationRequest request,
            CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent registration start timed out");
        }
        return registrationService.register(request);
    }
}
