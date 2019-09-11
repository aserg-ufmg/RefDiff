library(ggplot2)
library(plyr)

# disable scientific notation
options(scipen=999)

df=read.table("time.txt", header=TRUE)

# compute the median
dfMedian=ddply(df, .(tool), summarise, med = median(time))

ggplot(df, aes(x=tool, y=time)) + scale_y_log10() + ylab("Time (ms)") + xlab(element_blank()) +
  geom_violin() +
  stat_summary(fun.y="median", geom="point") +
  geom_text(data=dfMedian, aes(x=tool, y=med, label=med), size=3, vjust=-1.5)
