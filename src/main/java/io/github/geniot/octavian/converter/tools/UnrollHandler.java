package io.github.geniot.octavian.converter.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnrollHandler {

    static Logger logger = LoggerFactory.getLogger(UnrollHandler.class);


    public void unroll(UnrollContext ctx) throws Exception {
        if (ctx.isEndReached()) {
            return;
        }

//        logger.info(ctx.toString());
//
//        if (ctx.toString().equals("1_2_1")) {
//            System.out.println("Debug stop");
//        }

        if (ctx.isVolta(ctx.currentMeasure)) {
            ctx.registerVolta();
        }

        if (ctx.shouldSkipVolta()) {
            ctx.skipVolta();
            unroll(ctx);
            return;
        }

        ctx.add();

        if (ctx.isStartRepeat(ctx.currentMeasure) && ctx.isNewStartRepeat()) {
            ctx.registerStartRepeat();
            ctx.setCurrentRepeat(1);
        }

        if (ctx.isEndRepeat(ctx.currentMeasure) && ctx.shouldRepeat()) {
            ctx.registerEndRepeat();
            ctx.goToStartRepeat();
            ctx.setCurrentRepeat(ctx.getCurrentRepeat() + 1);
            unroll(ctx);
            return;
        }

        if (ctx.hasJump()) {
            ctx.registerJump();
            if (ctx.shouldJump()) {
                //#38
                if (ctx.currentJump.isShouldPlayRepeats()) {
                    ctx.resetRepeats();//maybe reset only inner repeats?
                }
                ctx.jump();
                unroll(ctx);
                return;
            }
        }

        if (ctx.isPlayUntilReached()) {
            if (ctx.shouldContinueAt()) {
                ctx.goToContinueAt();
                ctx.setCurrentRepeat(1);
                unroll(ctx);
            }
            return;
        }

        ctx.checkRecursion();

        ctx.next();
        unroll(ctx);
    }

}
