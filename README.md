# javaNG
Use Nei-Gojoborit algorithm to calculate Ka/Ks.

What is Ka/Ks? The ratio of the number of nonsynonymous substitutions per nonsynonymous site (Ka) to the number of synonymous substitutions per synonymous site (Ks). And it can diagnose the form of sequence evolution.

Generally speaking, if Ka is much greater than Ks(i.e. Ka/Ks >> 1), this is strong evidence that selection has acted to change the protein (positive selection).
Similarly, if Ka is much smaller than Ks(i.e. Ka/Ks << 1), that is, most of the time selection eliminates deleterious mutations, keeping the protein as it is (purifying selection).
However, if Ka equals Ks, the evolution of the sequence may be neutral, may be not, becuase there is anaother situation that one part of the gene (one protein domain, say) was under positive selection, but other parts under purifying selection. So we need to use other method to verify the evolution.

## Launch program

```
java -jar javaGUI.jar
```

## Usage

See "Help -> User guide" menu in the program.

## More information

See "Help -> About" menu in the program.

## Oct20,2022 update

see release.
