library(ggplot2)
library(plyr)

# disable scientific notation
options(scipen=999)

df=read.table("time.txt", header=TRUE)

# compute the median
dfMedian=ddply(df, .(tool), summarise, med = median(time))

plot1=ggplot(df, aes(x=tool, y=time)) + scale_y_log10() + ylab("Time (ms)") + xlab(element_blank()) +
  geom_violin() +
  stat_summary(fun.y="median", geom="point") +
  geom_text(data=dfMedian, aes(x=tool, y=med, label=med), size=3, vjust=-1.5)

ggsave("plot1.pdf", plot=plot1, width=4, height=4, units="in", dpi=300)


#dfRefdiff=read.table("refdiff.txt", header=TRUE)
#dfRminer=read.table("rminer.txt", header=TRUE)

#ggplot(dfRminer, aes(x=files, y=time)) + geom_point(shape=23) + geom_point(data=dfRefdiff, aes(x=files, y=time), shape=1, color="blue")
