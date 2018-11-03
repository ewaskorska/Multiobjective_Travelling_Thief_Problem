import java.util.ArrayList;
import java.util.Collections;

public class ParetoFrontsGenerator {

    Point ideal, nadir;

    private ArrayList<ArrayList<Individual>> paretoFronts = new ArrayList<>();
    public ParetoFrontsGenerator(Point ideal, Point nadir) {
        this.ideal = ideal;
        this.nadir = nadir;
    }

    //each calling overrides last set paretoFronts
    public ArrayList<ArrayList<Individual>> generateFronts(ArrayList<Individual> group) {

        paretoFronts = new ArrayList<>();
        paretoFronts.add(new ArrayList<>());
        for(int i = 0; i < group.size(); i++) {
            for (int j = 0; j < paretoFronts.size(); j++) {
                ArrayList<Individual> currentFront = paretoFronts.get(j);
                if (currentFront.size() == 0) {
                    currentFront.add(group.get(i));
                    break;
                } else {
                    for (int k = 0; k < currentFront.size(); k++) {
                        int compared = group.get(i).compareTo(currentFront.get(k));
                        if ((compared == 0) && (k == currentFront.size() - 1)) {
                            currentFront.add(group.get(i));
                            if(i < group.size() - 1) {
                                i++;
                                j = -1;
                            }else {
                                j = paretoFronts.size();
                            }
                            break;
                        } else if (compared == -1) {
                            //zamiana miejsc
                            ArrayList<Individual> betterFront = new ArrayList<>();
                            betterFront.add(group.get(i));
                            for(int z = 0; z < k; ) {
                                betterFront.add(currentFront.get(z));
                                currentFront.remove(z);
                                k--;
                            }
                            for(int z = 1; z < currentFront.size(); z++) {
                                if(group.get(i).compareTo(currentFront.get(z)) == 0) {
                                    betterFront.add(currentFront.get(z));
                                    currentFront.remove(z);
                                    z--;
                                }
                            }
                            paretoFronts.add(j, betterFront);
                            if(i < group.size() - 1) {
                                i++;
                                j = -1;
                            }else {
                                j = paretoFronts.size();
                            }
                            break;
                        } else if (compared == 1) {
                            //nowy front
                            if (paretoFronts.size() < j + 2) {
                                paretoFronts.add(new ArrayList<>());
                                paretoFronts.get(j + 1).add(group.get(i));
                                if(i < group.size() - 1) {
                                    i++;
                                    j = -1;
                                }else {
                                    j = paretoFronts.size();
                                }
                                break;
                            }
                            else {
                                break;
                            }
                        }
                    }
                }
            }
        }
        assignRank();
        crowdingDistanceSetter();
        return paretoFronts;
    }

    private void assignRank() {
        for(int i = 0; i < paretoFronts.size(); i++) {
            for(int j = 0; j < paretoFronts.get(i).size(); j++) {
                paretoFronts.get(i).get(j).setRank(i);
            }
        }
    }

    private void objectiveSorting() {
        for(int i = 0; i < paretoFronts.size(); i++) {
            Collections.sort(paretoFronts.get(i), new ObjectiveFrontComparator());
        }
    }

    private void crowdingDistanceSetter() {
        objectiveSorting();
        for(int i = 0; i < paretoFronts.size(); i++) {
            paretoFronts.get(i).get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
            paretoFronts.get(i).get(paretoFronts.get(i).size() - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

            for(int j = 1; j < paretoFronts.get(i).size() - 1; j++) {
                Individual currentInd = paretoFronts.get(i).get(j);
                double a = Math.abs(paretoFronts.get(i).get(j + 1).getFitnessTime()
                        - paretoFronts.get(i).get(j - 1).getFitnessTime());
                double b = Math.abs(paretoFronts.get(i).get(0).getFitnessWage()
                        - paretoFronts.get(i).get(paretoFronts.get(i).size() - 1).getFitnessWage());
                currentInd.setCrowdingDistance(a * b);
            }
        }
    }

    public String printPF(ArrayList<Individual> a, StringBuilder sB) {
        for(Individual i : a) {
            sB.append(i.getFitnessTime() + ", ");
            sB.append(i.getFitnessWage() + ", ");
            sB.append("\n");
        }
        return sB.toString();
    }

    public String ED_measure(ArrayList<Individual> group) {
        double sumED = 0;
        for(int i = 0; i < group.size(); i++) {
            Individual ind = group.get(i);
            sumED += Math.round(Math.sqrt(Long.valueOf((int)((ind.getFitnessTime() - ideal.x)
                    * (ind.getFitnessTime() - ideal.x) + (ind.getFitnessWage() - ideal.y)
                    * (ind.getFitnessWage() - ideal.y)))));
        }
        sumED = (sumED / group.size());

        return Math.round(sumED) + "";
    }

    public String PFS_measure() {
        return paretoFronts.get(0).size() + "";//docelowo archive
    }

    public double HV_measure() {
        Collections.sort(paretoFronts.get(0), new ObjectiveFrontComparator());//docelowo archive
        Long hyperVolume = 0L;
        double lastY = nadir.y;
        for(int i = 0; i < paretoFronts.get(0).size(); i++) {
            hyperVolume += ((int)((nadir.x - paretoFronts.get(0).get(i).getFitnessTime())
                    * (lastY - paretoFronts.get(0).get(i).getFitnessWage())));
            lastY = paretoFronts.get(0).get(i).getFitnessWage();
        }
        return hyperVolume;
    }

    public ArrayList<ArrayList<Individual>> getParetoFronts() {
        return paretoFronts;
    }
}
