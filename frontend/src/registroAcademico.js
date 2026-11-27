// Marcador de participante externo (F8). Repete RegistroAcademico.EXTERNO do backend:
// ao mudar a regra, mude os dois lugares.
export const RA_EXTERNO = 'EXTERNO';

// Mesma regra do backend: sem diferenciar maiúsculas de minúsculas e sem espaços nas bordas.
export const ehExterno = (ra) => String(ra ?? '').trim().toUpperCase() === RA_EXTERNO;
