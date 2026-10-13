import dev.unstable.underworld.UnderworldMath;
import java.util.*;
public final class VisualRegionsTest {
  public static void main(String[] args) {
    long seed=0x12553a7L;
    Set<Integer> zones=new HashSet<>();
    int roads=0, total=0;
    for (int x=-2400;x<2400;x+=19) {
      for (int z=-2400;z<2400;z+=19) {
        int zone=UnderworldMath.visualBiome(x,z,seed);
        if (zone<0 || zone>4) throw new AssertionError("invalid visual region");
        zones.add(zone);
        if (UnderworldMath.basaltTrail(x,z,seed,zone)) roads++;
        total++;
        if (UnderworldMath.visualBiome(x,z,seed)!=zone)
          throw new AssertionError("non-deterministic province");
      }
    }
    if (zones.size()!=5) throw new AssertionError("expected all five zones, found "+zones);
    if (roads<30 || roads > total/4) throw new AssertionError("bad trail prevalence "+roads+"/"+total);
    System.out.println("PASS: 5 visual provinces, "+roads+" basalt-trail samples of "+total);
  }
}
