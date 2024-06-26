package com.example.justtest.alarm;

import com.example.justtest.reviewcrud.Repository;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class SseEmitters {
  private static final AtomicLong counter = new AtomicLong();
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  private final Repository repository;

  public SseEmitters(Repository repository) {
    this.repository = repository;
  }

  SseEmitter add(SseEmitter emitter) {
    this.emitters.add(emitter);
    log.info("new emitter added: {}", emitter);
    log.info("emitter list size: {}", emitters.size());
    emitter.onCompletion(() -> {
      log.info("onCompletion callback");
      this.emitters.remove(emitter);    // 만료되면 리스트에서 삭제
    });
    emitter.onTimeout(() -> {
      log.info("onTimeout callback");
      emitter.complete();
    });

    return emitter;
  }
  public void count() {
    long count = counter.incrementAndGet();
    emitters.forEach(emitter -> {
      try {
        String a = repository.findById(count).get().getDescription();
        emitter.send(SseEmitter.event()
            .name("count")
            .data(a));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

}
