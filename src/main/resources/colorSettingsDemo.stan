/*
 * Hierarchical normal model — Stan demo
 */
functions {
  real log_half_cauchy(real x, real scale) {
    return cauchy_lpdf(x | 0, scale) - cauchy_lccdf(0 | 0, scale);
  }
}

data {
  int<lower=1> N;        // number of observations
  int<lower=1> J;        // number of groups
  array[N] real y;       // observations
  array[N] int<lower=1, upper=J> group;
}

parameters {
  real mu_hyper;
  real<lower=0> sigma_hyper;
  vector[J] mu;
  real<lower=0> sigma;
}

model {
  // Hyper-priors
  mu_hyper ~ normal(0, 10);
  target += log_half_cauchy(sigma_hyper, 2.5);

  // Group-level priors
  mu ~ normal(mu_hyper, sigma_hyper);
  target += log_half_cauchy(sigma, 2.5);

  // Likelihood
  for (n in 1:N)
    y[n] ~ normal(mu[group[n]], sigma);
}

generated quantities {
  real y_rep = normal_rng(mu[1], sigma);
}
