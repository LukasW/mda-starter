export type VertragsTyp =
  | 'LIEFERANTENVERTRAG'
  | 'KUNDENVERTRAG'
  | 'ARBEITSVERTRAG'
  | 'KOOPERATIONSVERTRAG'
  | 'SONSTIGES';

export type VertragStage =
  | 'ENTWURF'
  | 'IN_PRUEFUNG'
  | 'KORREKTURBEDARF'
  | 'FREIGEGEBEN'
  | 'ZUR_SIGNATUR'
  | 'UNTERZEICHNET'
  | 'ARCHIVIERT'
  | 'ABGELAUFEN'
  | 'GEKUENDIGT';

export type ParteiRolle =
  | 'AUFTRAGGEBER'
  | 'AUFTRAGNEHMER'
  | 'UNTERZEICHNER'
  | 'INFORMIERT'
  | 'VERANTWORTLICH';

export interface VertragDto {
  id: string;
  titel: string;
  typ: VertragsTyp;
  stage: VertragStage;
  gueltigVon: string | null;
  gueltigBis: string | null;
  erstellerId: string;
  versionNumber: number;
  parteien: Array<{ rolle: ParteiRolle; personId: string }>;
  versionen: Array<{
    versionNummer: number;
    erstellt: string;
    erstelltVon: string;
    mimeType: string;
    groesseByte: number;
    speicherTyp: string;
    inhaltHash: string | null;
  }>;
}

export interface PersonDto {
  id: string;
  vorname: string;
  nachname: string;
  email: string;
  organisation: string | null;
  funktion: string | null;
  quelleTyp: 'INTERN' | 'EXTERN_API';
  externeId: string | null;
  versionNumber: number;
}

export interface ApiError {
  code: string;
  status: number;
  message: string;
  fieldErrors: Array<{ field: string; code: string; message: string }>;
}

export interface ProblemDetail {
  type?: string;
  title: string;
  status: number;
  code: string;
  detail?: string;
  errors?: Array<{ field: string; code: string; message: string }>;
}
