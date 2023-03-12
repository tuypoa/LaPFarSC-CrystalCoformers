import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np

#sns.set_theme(style="ticks")
sns.set_theme(style="darkgrid")

summary = pd.read_csv('SUMMARY.txt', sep=';', keep_default_na=True, usecols = ['mol1','mol2','relax'])
summary = summary.dropna()
summary['relax'] = np.where(summary['relax'], 'SCF converged', '')
#summary.loc[:, 'mol1'] = summary['mol1'] + np.random.uniform(-0.5, 0.5) 
summary['mol1'] = summary['mol1'].apply(lambda x: x + np.random.uniform(-0.1, 0.2) )
summary['mol2'] = summary['mol2'].apply(lambda x: x + np.random.uniform(-0.2, 0.2) )

#summary.info()
#sns.histplot(summary.mol1, kde=True)

#g = sns.jointplot(x="mol1", y="mol2", data=summary,
#                  kind="reg", truncate=False,
#                  xlim=(0, 10), ylim=(0, 10),
#                  color="m", height=7)
               

#sns.jointplot(data=summary, x="mol2", y="mol1", kind="hist", xlim=(0, 10), ylim=(0, 6), color="m", height=5)\

g = sns.JointGrid(data=summary, x="mol2", y="mol1", hue="relax", height=5, ratio=3, space=.2, xlim=(-0.5, 9), ylim=(-0.5, 6), marginal_ticks=True, palette=['white','blue'])
g.plot(sns.scatterplot, sns.histplot, alpha=.7, edgecolor=".2", linewidth=.5)
g.refline(x=1, y=0.7)

sns.move_legend(g.ax_joint, "upper right", title=None, frameon=False)
g.set_axis_labels(xlabel='Lglutathione molecules', ylabel='Macitentan  molecules')

plt.savefig('graph-ratio.png')
