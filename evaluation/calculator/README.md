Compare the detection result with the oracle and calculate precision, recall.
Output the result that merges the detection result and the oracle as a CSV file.
Basically, run this script after running detection for oracle data.

```bash
python3 -m venv venv
source venv/bin/activate
python3 -m src.main -l java -d ../detection-result/1027-1130-java.csv
venv/bin/python -m src.main -l c -m precision -d ../detection-result/1027-0837-c-precision.csv
# or venv/bin/python
```

```bash
python3 -m unittest
```

## Random Sampling
To randomly sample from the merged result CSV file, use `random.py` which outputs a randomly sampled CSV file to `/calculator/sampled/sampled-{lang}-head-{date}-seed{seed}.csv`.

```bash
venv/bin/python -m src.manual.random
```