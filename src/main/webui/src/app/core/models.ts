export type Vertragsart =
  | 'DIENSTLEISTUNG'
  | 'MIETE'
  | 'LIZENZ'
  | 'RUECKVERSICHERUNG'
  | 'ARBEIT';

export const VERTRAGSARTEN: readonly Vertragsart[] = [
  'DIENSTLEISTUNG',
  'MIETE',
  'LIZENZ',
  'RUECKVERSICHERUNG',
  'ARBEIT',
] as const;

export type VertragStatus =
  | 'ENTWURF'
  | 'IN_PRUEFUNG'
  | 'UEBERARBEITUNG'
  | 'FREIGEGEBEN'
  | 'AKTIV'
  | 'IN_KUENDIGUNG'
  | 'BEENDET'
  | 'ABGELAUFEN'
  | 'ARCHIVIERT';

export type FristArt = 'KUENDIGUNG' | 'VERLAENGERUNG' | 'ABLAUF' | 'INDIVIDUELL';
export const FRIST_ARTEN: readonly FristArt[] = [
  'KUENDIGUNG',
  'VERLAENGERUNG',
  'ABLAUF',
  'INDIVIDUELL',
] as const;

export type FristStatus = 'OFFEN' | 'ERINNERT' | 'ERLEDIGT' | 'ESKALIERT';

export type Entscheidung = 'GENEHMIGT' | 'ABGELEHNT';

export interface DokumentVersionDto {
  versionId: string;
  versionNummer: number;
  dateiname: string;
  mimeType: string;
  groesseBytes: number;
  pruefsummeSha256: string;
  hochgeladenAm: string;
}

export interface VertragDto {
  vertragId: string;
  mandantId: string;
  titel: string;
  vertragsart: Vertragsart;
  partnerId: string;
  status: VertragStatus;
  startDatum: string | null;
  endDatum: string | null;
  kuendigungsfristTage: number | null;
  versionen: DokumentVersionDto[];
}

export interface FristDto {
  fristId: string;
  vertragId: string;
  art: FristArt;
  faelligkeitsDatum: string;
  vorlaufTage: number;
  erinnerungsDatum: string;
  status: FristStatus;
  verantwortlicherUserId: string;
}

export interface FreigabeDto {
  freigabeId: string;
  vertragId: string;
  versionId: string;
  reviewerId: string;
  entscheidung: Entscheidung | null;
  begruendung: string | null;
  angefordertAm: string;
  entschiedenAm: string | null;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  code: string;
}

export interface ErfassenVertragRequest {
  mandantId: string;
  titel: string;
  vertragsart: Vertragsart;
  partnerId: string;
  startDatum?: string | null;
  endDatum?: string | null;
  kuendigungsfristTage?: number | null;
  antragstellerId: string;
}

export interface ErfassenFristRequest {
  vertragId: string;
  art: FristArt;
  faelligkeitsDatum: string;
  vorlaufTage: number;
  verantwortlicherId: string;
}
