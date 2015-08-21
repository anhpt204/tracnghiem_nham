All parameters and problem definition are in file: common/Const.java.

pakages agx, rdo, sc, sgx, ssgx implement AGX, RDO, SC, SGX, SSGX methods respectively. The details of these implementations are in file Population.java of each packages.

How to run?
1. cd to the gpem15 directory
2. edit parameters and problem definition in common/Const.java.
2. run: javac @sources.txt 
3. run:
- SC: 
java -cp path-to-src sc.MainSC

- AGX
java -cp path-to-src agx.MainAGX

-RDO:
java -cp path-to-src agx.MainAGX

-SGX:
java -cp path-to-src-folder sgx.MainSGX

-SSGX:
java -cp path-to-src ssgx.MainSSGX

