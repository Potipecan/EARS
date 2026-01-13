import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt

stats_df = pd.read_csv('outputs/stats.csv')
errors_df = pd.read_csv('outputs/errors.csv')

plot = errors_df.plot.box()
plot.set_title("Porazdelitev napak")
plot.set_ylabel("Napaka")
plot.set_xlabel("Rešitev")
plt.tight_layout()
plt.savefig('outputs/plots/err_dist.png')
plt.show()


fig, axes = plt.subplots(2, 1)
fig.suptitle("Kompleksnost in fitnes rešitev")
plot = stats_df.plot.bar(x="solution", y=["n_functions", "n_terminals"], ax=axes[0])
plot.set_title("Število vozlišč")
plot.set_xlabel("Rešitev")
plot.set_ylabel("")

for container in plot.containers:
    plot.bar_label(container, rotation=90, padding=5.0, fmt="%d")

plot = stats_df.plot.bar(x="solution", y=["f_train","f_test"], ax=axes[1])
plot.set_title("Fitnes rešitev")
plot.set_xlabel("Rešitev")
plot.set_ylabel("Fitnes")

for container in plot.containers:
    plot.bar_label(container, rotation=90, padding=5.0, fmt="%.2f")

plt.tight_layout()
plt.savefig('outputs/plots/stats.png')
plt.show()