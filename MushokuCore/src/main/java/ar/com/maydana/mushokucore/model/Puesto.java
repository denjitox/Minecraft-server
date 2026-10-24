package ar.com.maydana.mushokucore.model;

/**
 * Puesto de las Siete Potencias — modelo inmutable (plan §6.2, F0).
 * Sin lógica. {@code posicion} ∈ 1–7 (CHECK de {@code world_powers}, spec §10 L468);
 * títulos §11 L541-548. Los puestos 1/2/3/5 son los operativos del MVP (§3 L97);
 * 4/6/7 llegan en 1.3 (§3 L131).
 *
 * @param posicion número del puesto (1–7)
 * @param titulo   título del puesto (ej. "El Primer Hechicero")
 */
public record Puesto(int posicion, String titulo) {
}
