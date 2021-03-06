
package com.airhacks.wad.watch.boundary;

import com.airhacks.wad.watch.control.Builder;
import com.airhacks.wad.watch.control.Copier;
import com.airhacks.wad.watch.control.FolderWatchService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.maven.shared.invoker.InvocationResult;

/**
 *
 * @author airhacks.com
 */
public class WADFlow {

    private final static AtomicLong successCounter = new AtomicLong();
    private final Builder builder;

    public WADFlow(Path dir, Path war, List<Path> deploymentTargets) throws IOException {
        this.builder = new Builder();
        Runnable changeListener = () -> buildAndDeploy(war, deploymentTargets);
        changeListener.run();
        FolderWatchService.listenForChanges(dir, changeListener);
    }

    void buildAndDeploy(Path war, List<Path> deploymentTargets) {
        long start = System.currentTimeMillis();
        try {
            InvocationResult result = this.builder.build();
            if (result.getExitCode() == 0) {
                System.out.printf("[%d] ", successCounter.incrementAndGet());
                System.out.print("\uD83D\uDC4D");
                System.out.println(" built in " + (System.currentTimeMillis() - start) + " ms");
                start = System.currentTimeMillis();
                Copier.copy(war, deploymentTargets);
                System.out.print("\uD83D\uDE80 ");
                System.out.println(" copied in " + (System.currentTimeMillis() - start) + " ms");
            } else {
                System.out.print("\uD83D\uDC4E ");
                System.err.println(" : " + result.getExecutionException().getMessage());
            }
        } catch (Exception ex) {
            System.err.println(ex.getClass().getName() + " " + ex.getMessage());
        }
    }

}
