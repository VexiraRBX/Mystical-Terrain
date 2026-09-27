package com.vexira.mysticalterrain.formation;

/**
 * Already sampled. size is the main dimension of the kind (a spire's height, a crater's radius...), count of -1 means
 * whatever suits it, and girth scales how thick everything is.
 */
public record FormationParams(FormationKind kind, int size, int count, double lean, double girth, int sides, long seed) {
}
